package com.autoxpress.service;

import com.autoxpress.model.CarModel;
import com.autoxpress.model.ReportModel;
import com.autoxpress.model.UserModel;
import com.autoxpress.repository.ReportRepository;
import com.autoxpress.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void saveReport(ReportModel report, CarModel car, String username) throws Exception {
        Optional<UserModel> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new Exception("User not found.");
        }
        report.setReportUser(user.get());
        report.setReportCar(car);
        report.setReportStatus(ReportModel.ReportStatus.PENDING);
        report.setReportCreatedAt(LocalDateTime.now());
        report.setReportUpdatedAt(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Transactional
    public ReportModel resolveReport(Long reportId) throws Exception {
        ReportModel report = reportRepository.findById(reportId)
                .orElseThrow(() -> new Exception("Report not found."));
        report.setReportStatus(ReportModel.ReportStatus.RESOLVED);
        report.setReportUpdatedAt(LocalDateTime.now());
        return reportRepository.save(report);
    }

    public List<ReportModel> getAllReports() {
        return reportRepository.findAll();
    }
}