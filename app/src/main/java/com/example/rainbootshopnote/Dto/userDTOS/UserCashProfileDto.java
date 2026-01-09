package com.example.rainbootshopnote.Dto.userDTOS;


import com.example.rainbootshopnote.Dto.CashDTO;

import java.util.ArrayList;
import java.util.List;

public class UserCashProfileDto {

    private int userId;
    private String userName;

    // Liste
    private List<CashDTO> cashItems = new ArrayList<>();

    // Toplamlar
    private double totalDeposits;     // giriş toplamı
    private double totalWithdrawals;  // çıkış toplamı
    private double net;              // deposits - withdrawals

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public List<CashDTO> getCashItems() { return cashItems; }
    public void setCashItems(List<CashDTO> cashItems) { this.cashItems = cashItems; }

    public double getTotalDeposits() { return totalDeposits; }
    public void setTotalDeposits(double totalDeposits) { this.totalDeposits = totalDeposits; }

    public double getTotalWithdrawals() { return totalWithdrawals; }
    public void setTotalWithdrawals(double totalWithdrawals) { this.totalWithdrawals = totalWithdrawals; }

    public double getNet() { return net; }
    public void setNet(double net) { this.net = net; }
}



//public com.example.rainbootshopnote.Dto.UserCashProfileDto getUserCashProfileDto(int userId) {
//    com.example.rainbootshopnote.Dto.UserCashProfileDto dto =
//            new com.example.rainbootshopnote.Dto.UserCashProfileDto();
//
//    dto.setUserId(userId);
//
//    android.database.sqlite.SQLiteDatabase db = this.getReadableDatabase();
//
//    // 1) user name
//    android.database.Cursor uCur = null;
//    try {
//        uCur = db.rawQuery("SELECT name FROM users WHERE user_id = ? AND deleteStatus = 0",
//                new String[]{String.valueOf(userId)});
//        if (uCur.moveToFirst()) {
//            dto.setUserName(uCur.getString(0));
//        } else {
//            dto.setUserName("");
//        }
//    } finally {
//        if (uCur != null) uCur.close();
//    }
//
//    // 2) cash list (deposits + withdrawals)
//    String q =
//            "SELECT * FROM (" +
//                    "  SELECT d.deposit_id AS itemId, d.user_id AS userId, d.name AS name, d.amount AS price," +
//                    "         d.create_date AS create_date, d.description AS description, d.status AS status," +
//                    "         1 AS isDeposit" +
//                    "    FROM deposits d" +
//                    "   WHERE d.deleteStatus = 0 AND d.user_id = ? " +
//                    "  UNION ALL " +
//                    "  SELECT w.withdrawal_id AS itemId, w.user_id AS userId, w.name AS name, w.amount AS price," +
//                    "         w.create_date AS create_date, w.description AS description, w.status AS status," +
//                    "         0 AS isDeposit" +
//                    "    FROM withdrawals w" +
//                    "   WHERE w.deleteStatus = 0 AND w.user_id = ? " +
//                    ") ORDER BY create_date DESC";
//
//    android.database.Cursor c = null;
//    java.util.List<com.example.rainbootshopnote.Dto.CashDTO> items = new java.util.ArrayList<>();
//    double sumIn = 0.0;
//    double sumOut = 0.0;
//
//    try {
//        c = db.rawQuery(q, new String[]{String.valueOf(userId), String.valueOf(userId)});
//        while (c.moveToNext()) {
//            com.example.rainbootshopnote.Dto.CashDTO x = new com.example.rainbootshopnote.Dto.CashDTO();
//
//            int itemId = c.getInt(c.getColumnIndexOrThrow("itemId"));
//            String name = c.getString(c.getColumnIndexOrThrow("name"));
//            double price = c.getDouble(c.getColumnIndexOrThrow("price"));
//            String createDate = c.getString(c.getColumnIndexOrThrow("create_date"));
//            String description = c.getString(c.getColumnIndexOrThrow("description"));
//            boolean status = c.getInt(c.getColumnIndexOrThrow("status")) == 1;
//            boolean isDeposit = c.getInt(c.getColumnIndexOrThrow("isDeposit")) == 1;
//
//            // CashDTO mapping (senin CashDTO alan adların projende mevcut)
//            x.setItemId(itemId);
//            x.setName(name);
//            x.setPrice((float) price);
//            x.setCreateDate(createDate);
//            x.setDescription(description);
//            x.setStatus(status);
//            x.setDepositOrWithdrawal(isDeposit);
//
//            items.add(x);
//
//            // toplamları sadece status=true olanlardan istiyorsan:
//            if (status) {
//                if (isDeposit) sumIn += price;
//                else sumOut += price;
//            }
//        }
//    } finally {
//        if (c != null) c.close();
//        db.close();
//    }
//
//    dto.setCashItems(items);
//    dto.setTotalDeposits(sumIn);
//    dto.setTotalWithdrawals(sumOut);
//    dto.setNet(sumIn - sumOut);
//
//    return dto;
//}
