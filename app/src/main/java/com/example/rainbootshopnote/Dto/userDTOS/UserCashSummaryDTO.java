package com.example.rainbootshopnote.Dto.userDTOS;

import androidx.annotation.Nullable;

import com.example.rainbootshopnote.Dto.CashDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile-level cash summary.
 * - totals are computed from rows where status=1 (consistent with UI short-info behavior)
 */
public class UserCashSummaryDTO {

    private int userId;
    private float totalDeposits;
    private float totalWithdrawals;
    private float net; // deposits - withdrawals

    @Nullable
    private String fromDay;
    @Nullable
    private String toDay;

    private List<CashDTO> rows = new ArrayList<>();

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public float getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(float totalDeposits) { this.totalDeposits = totalDeposits; }

    public float getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(float totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }

    public float getNet() { return net; }
    public void setNet(float net) { this.net = net; }

    @Nullable
    public String getFromDay() { return fromDay; }
    public void setFromDay(@Nullable String fromDay) { this.fromDay = fromDay; }

    @Nullable
    public String getToDay() { return toDay; }
    public void setToDay(@Nullable String toDay) { this.toDay = toDay; }

    public List<CashDTO> getRows() { return rows; }
    public void setRows(List<CashDTO> rows) { this.rows = rows; }
}
