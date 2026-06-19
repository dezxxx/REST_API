package com.dezxxx.rest.service;

import com.dezxxx.rest.exception.EntityNotFoundException;
import com.dezxxx.rest.model.Event;
import com.dezxxx.rest.model.File;
import com.dezxxx.rest.model.User;
import com.dezxxx.rest.repository.EventRepository;
import com.dezxxx.rest.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventServiceImpl(eventRepository);
    }

    @Test
    void create_shouldReturnCreatedEvent() {
        Event event = new Event(new User("Ivan"), new File("report.pdf", "/uploads/report.pdf"));
        when(eventRepository.create(event)).thenReturn(event);

        Event result = eventService.create(event);

        assertEquals(event, result);
        verify(eventRepository).create(event);
    }

    @Test
    void findById_shouldReturnEvent_whenExists() {
        Event event = new Event();
        event.setId(1);
        when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        Optional<Event> result = eventService.findById(1);

        assertTrue(result.isPresent());
        assertEquals(event, result.get());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        Optional<Event> result = eventService.findById(99);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAll_shouldReturnAllEvents() {
        List<Event> events = List.of(new Event(), new Event());
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.findAll();

        assertEquals(2, result.size());
        verify(eventRepository).findAll();
    }

    @Test
    void findByUserId_shouldReturnUserEvents() {
        List<Event> events = List.of(new Event());
        when(eventRepository.findByUserId(1)).thenReturn(events);

        List<Event> result = eventService.findByUserId(1);

        assertEquals(1, result.size());
        verify(eventRepository).findByUserId(1);
    }

    @Test
    void update_shouldThrowEntityNotFoundException_whenNotExists() {
        Event event = new Event();
        event.setId(99);
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.update(event));
        verify(eventRepository, never()).update(any());
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenNotExists() {
        when(eventRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> eventService.delete(99));
        verify(eventRepository, never()).delete(any());
    }
}
