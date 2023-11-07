package com.fypgrading.adminservice.controller;

import com.fypgrading.adminservice.service.TeamService;
import com.fypgrading.adminservice.service.dto.CountDTO;
import com.fypgrading.adminservice.service.dto.TeamDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping("/")
    public ResponseEntity<List<TeamDTO>> getTeams() {
        List<TeamDTO> teams = teamService.getTeams();
        return ResponseEntity.ok().body(teams);
    }

    @GetMapping("/teamds/{id}/reviewers")
    public ResponseEntity<CountDTO> getTeamReviewers(@PathVariable Integer id) {
        CountDTO reviewersCount = teamService.getTeamReviewers(id);
        return ResponseEntity.ok().body(reviewersCount);
    }

    @PostMapping("/")
    public ResponseEntity<TeamDTO> createTeam(@RequestBody TeamDTO teamDTO) {
        TeamDTO createdTeam = teamService.createTeam(teamDTO);
        return ResponseEntity.ok().body(createdTeam);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> updateTeam(@PathVariable Integer id, @RequestBody TeamDTO teamDTO) {
        TeamDTO teams = teamService.updateTeam(id, teamDTO);
        return ResponseEntity.ok().body(teams);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TeamDTO> deleteTeam(@PathVariable Integer id) {
        TeamDTO teams = teamService.deleteTeam(id);
        return ResponseEntity.ok().body(teams);
    }
}
