package tn.esprit.eventsproject.services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import  tn.esprit.eventsproject.entities.Participant;
import  tn.esprit.eventsproject.entities.Tache;
import  tn.esprit.eventsproject.repositories.EventRepository;
import  tn.esprit.eventsproject.repositories.LogisticsRepository;
import  tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.*;

class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddParticipant() {
        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setNom("Test");

        when(participantRepository.save(participant)).thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertNotNull(result);
        assertEquals("Test", result.getNom());
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipantWithId() {
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Test Event");

        Participant participant = new Participant();
        participant.setIdPart(1);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(result);
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipantWithParticipants() {
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Test Event");

        Participant participant = new Participant();
        participant.setIdPart(1);

        Set<Participant> participants = new HashSet<>();
        participants.add(participant);
        event.setParticipants(participants);

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(event)).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event);

        assertNotNull(result);
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog() {
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Test Event");

        Logistics logistics = new Logistics();
        logistics.setIdLog(1);

        when(eventRepository.findByDescription("Test Event")).thenReturn(event);
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, "Test Event");

        assertNotNull(result);
        assertTrue(event.getLogistics().contains(logistics));
        verify(eventRepository, times(1)).findByDescription("Test Event");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates() {
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 10);

        Event event = new Event();
        event.setIdEvent(1);

        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        Logistics logistics2 = new Logistics();
        logistics2.setReserve(false);

        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics1);
        logisticsSet.add(logistics2);
        event.setLogistics(logisticsSet);

        List<Event> events = List.of(event);

        when(eventRepository.findByDateDebutBetween(startDate, endDate)).thenReturn(events);

        List<Logistics> result = eventServices.getLogisticsDates(startDate, endDate);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(logistics1));
        verify(eventRepository, times(1)).findByDateDebutBetween(startDate, endDate);
    }

    @Test
    void testCalculCout() {
        Event event = new Event();
        event.setIdEvent(1);
        event.setDescription("Test Event");

        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        logistics1.setPrixUnit(100);
        logistics1.setQuantite(2);

        Logistics logistics2 = new Logistics();
        logistics2.setReserve(false);

        Set<Logistics> logisticsSet = new HashSet<>();
        logisticsSet.add(logistics1);
        logisticsSet.add(logistics2);
        event.setLogistics(logisticsSet);

        List<Event> events = List.of(event);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(events);

        eventServices.calculCout();

        verify(eventRepository, times(1))
                .findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache("Tounsi", "Ahmed", Tache.ORGANISATEUR);
        verify(eventRepository, times(1)).save(event);
        assertEquals(200, event.getCout());
    }
}
