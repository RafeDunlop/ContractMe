package nz.ac.canterbury.seng302.homehelper.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Teams;
import nz.ac.canterbury.seng302.homehelper.repository.TeamsRepository;
import nz.ac.canterbury.seng302.homehelper.service.TeamsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

@ExtendWith(MockitoExtension.class)
public class TeamsServiceTest{

    @MockBean
    private TeamsRepository teamsRepository;
    private TeamsService teamsService;

    @BeforeEach
    void setUp() {
        teamsService = new TeamsService(teamsRepository);
    }

    @Test
    void saveTeam_callsRepositorySave() {
        Teams team = new Teams(new RenovationRecord());
        when(teamsRepository.save(team)).thenReturn(team);
        teamsService.saveTeam(team);
        verify(teamsRepository).save(team);
    }

    @Test
    void teamExists_whenTeamExists_returnsTrue() {
        Long renovationId = 1L;
        when(teamsRepository.existsByRenovationRecordId(renovationId)).thenReturn(true);
        boolean result = teamsService.teamExists(renovationId);
        assertTrue(result);
        verify(teamsRepository).existsByRenovationRecordId(renovationId);
    }

    @Test
    void teamExists_whenTeamDoesNotExist_returnsFalse() {
        Long renovationId = 1L;
        when(teamsRepository.existsByRenovationRecordId(renovationId)).thenReturn(false);
        boolean result = teamsService.teamExists(renovationId);
        assertFalse(result);
        verify(teamsRepository).existsByRenovationRecordId(renovationId);
    }

    @Test
    void teamExists_withNullId_returnsFalse() {
        when(teamsRepository.existsByRenovationRecordId(null)).thenReturn(false);
        boolean result = teamsService.teamExists(null);
        assertFalse(result);
        verify(teamsRepository).existsByRenovationRecordId(null);
    }
}