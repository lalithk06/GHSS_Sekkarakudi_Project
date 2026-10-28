package com.school.app.ranking;

import com.school.app.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/class/{sectionGroupId}")
    public ApiResponse<List<RankEntryDto>> rankByClass(@PathVariable Long sectionGroupId, @RequestParam Long examId) {
        return ApiResponse.ok(rankingService.rankBySectionGroupAndExam(sectionGroupId, examId));
    }

    @GetMapping("/standard/{standardId}")
    public ApiResponse<List<RankEntryDto>> rankByStandard(@PathVariable Long standardId, @RequestParam Long examId) {
        return ApiResponse.ok(rankingService.rankByStandardAndExam(standardId, examId));
    }
}
