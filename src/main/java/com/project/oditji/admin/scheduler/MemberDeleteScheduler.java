package com.project.oditji.admin.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.project.oditji.admin.service.AdminService;

@Component
public class MemberDeleteScheduler {

    private final AdminService adminService;

    public MemberDeleteScheduler(AdminService adminService) {
        this.adminService = adminService;
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void deleteExpiredWithdrawMembers() {

        adminService.deleteExpiredWithdrawMembers();

    }

}