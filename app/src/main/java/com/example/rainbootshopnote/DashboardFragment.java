package com.example.rainbootshopnote;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rainbootshopnote.Dto.ProductDTO;
import com.example.rainbootshopnote.Dto.UserDTO;
import com.example.rainbootshopnote.databaseConfiger.DBHelper;
import com.example.rainbootshopnote.entities.insertProducts;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DashboardFragment extends Fragment {

    private DBHelper db;
    private TextView price1, price2, price3, price4, price5, price6, price7, price8, price9, price10, price11, price12;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        price1 = view.findViewById(R.id.dashboardPrice1);
        price2 = view.findViewById(R.id.dashboardPrice2);
        price3 = view.findViewById(R.id.dashboardPrice3);
        price4 = view.findViewById(R.id.dashboardPrice4);
        price5 = view.findViewById(R.id.dashboardPrice5);
        price6 = view.findViewById(R.id.dashboardPrice6);
        price7 = view.findViewById(R.id.dashboardPrice7);
        price8 = view.findViewById(R.id.dashboardPrice8);
        price9 = view.findViewById(R.id.dashboardPrice9);
        price10 = view.findViewById(R.id.dashboardPrice10);
        price11 = view.findViewById(R.id.dashboardPrice11);
        price12 = view.findViewById(R.id.dashboardPrice12);

        TextInputEditText txtToDate = view.findViewById(R.id.txtToDate);
        TextInputEditText txtFromDate = view.findViewById(R.id.txtFromDate);

        // DecimalFormat tanımla
        DecimalFormat decimalFormat = new DecimalFormat("#.00");

        db = new DBHelper(getActivity());

        // Tüm ürünlerin toplam fiyatını hesapla
        double totalPrice = db.getAllProducts().stream()
                .mapToDouble(product -> product.getPrice() * product.getQuantity())
                .sum();
        String formattedTotalPrice = decimalFormat.format(totalPrice);
        price1.setText(formattedTotalPrice);


        // ProductDTO listesini al
        List<ProductDTO> productDTO = db.getProductDTOList();

        int totalQuantity4true = 0;
        double totalPrice4true = 0.0;
        int totalQuantity4 = 0;
        double totalPrice4 = 0.0;

        double minesWin = 0.0;
        double plusWin = 0.0;
        double iWin = 0.0;

        // ProductDTO listesinin toplamını hesapla
        for (ProductDTO product : productDTO) {
            if (product.isSaleOrInsert()){

                totalQuantity4true += product.getQuantity(); // Quantity toplamını hesapla
                totalPrice4true += product.getQuantity() * product.getPrice(); // Quantity ile Price çarpımını ekle

            }else {

                totalQuantity4 += product.getQuantity(); // Quantity toplamını hesapla
                totalPrice4 += product.getQuantity() * product.getPrice(); // Quantity ile Price çarpımını ekle
            }

            if (product.getWin() <0){
                minesWin += product.getWin();
            }else {
                plusWin += product.getWin();
            }
        }

        // totalPrice4'ü biçimlendir
        String formattedTotalPrice4true = decimalFormat.format(totalPrice4true);
        String formattedTotalPrice4 = decimalFormat.format(totalPrice4);

        price2.setText(String.valueOf(totalQuantity4));
        price3.setText(formattedTotalPrice4);

        price4.setText(String.valueOf(totalQuantity4true));
        price5.setText(formattedTotalPrice4true);

        String formattedminesWin = decimalFormat.format(minesWin);
        String formattedplusWin = decimalFormat.format(plusWin);
        String formattedIWin = decimalFormat.format(minesWin + plusWin);
        price6.setText(formattedminesWin);
        price7.setText(formattedplusWin);
        price8.setText(formattedIWin);


        double withdrawalAmount = 0.00;
        double depositAmount = 0.00;

        double depositPlus = 0.00;
        double withdrawalPlus = 0.00;

        // ProductDTO listesini al
        List<UserDTO> userDTO = db.getUsersWithAmounts();

        for (UserDTO user : userDTO) {
            withdrawalAmount += user.getWithdrawalsAmount();
            depositAmount +=user.getDepositAmount();

            if (user.getWithdrawalsAmount()>user.getDepositAmount()){
                depositPlus += user.getWithdrawalsAmount()-user.getDepositAmount();
            }else {
               withdrawalPlus +=  user.getDepositAmount() - user.getWithdrawalsAmount();
            }
        }

        String formattedwithdrawalAmount = decimalFormat.format(withdrawalAmount);
        String formatteddepositAmount = decimalFormat.format(depositAmount);
        String formattedwithdrawalPlus = decimalFormat.format(withdrawalPlus);
        String formatteddepositPlus = decimalFormat.format(depositPlus);
        price9.setText(formattedwithdrawalAmount);
        price10.setText(formatteddepositAmount);
        price11.setText(formattedwithdrawalPlus);
        price12.setText(formatteddepositPlus);

//        txtFromDate.setOnClickListener(v -> {
//            showDatePicker(null, (date) -> {
//                // Tarihi seç ve filtreyi uygula
//                String selectedFromDateStr = dateFormat.format(date);
//                txtFromDate.setText(selectedFromDateStr); // Text alanına tarihi yazdır
//
//                try {
//                    Date selectedFromDate = dateFormat.parse(selectedFromDateStr);
//
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            });
//        });
//
//        // txtToDate tıklanınca, txtFromDate seçildiyse iki tarih arasında filtreleme yap
//        txtToDate.setOnClickListener(v -> {
//            if (txtFromDate.getText().toString().isEmpty()) {
//                // Eğer txtFromDate seçilmediyse uyarı göster
//                Toast.makeText(getContext(), "تاریخ شروع را انتخاب کنید.", Toast.LENGTH_SHORT).show();
//            } else {
//                // txtToDate tıklanınca, başlangıç tarihi olarak txtFromDate'in tarihini ayarla
//                try {
//                    String fromDateStr = txtFromDate.getText().toString();
//                    Date fromDate = dateFormat.parse(fromDateStr);
//
//                    // txtToDate seçimi için DatePicker, minimum tarihi fromDate olarak ayarla
//                    showDatePicker(fromDate, (date) -> {
//                        txtToDate.setText(dateFormat.format(date)); // Set selected date to txtToDate
//
//                        String toDateStr = dateFormat.format(date); // Seçilen bitiş tarihi
//
//                        try {
//                            Date toDate = dateFormat.parse(toDateStr);
//
//
//
//                        } catch (ParseException e) {
//                            e.printStackTrace();
//                        }
//                    });
//
//                } catch (ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        return view;
    }

    // Tek Date Picker methodu (Optional minDate)
//    private void showDatePicker(@Nullable Date minDate, SaleFragment.OnDateSetListener listener) {
//        Calendar calendar = Calendar.getInstance();
//
//        DatePickerDialog datePickerDialog = new DatePickerDialog(
//                getContext(),
//                AlertDialog.THEME_HOLO_LIGHT,
//                (view, year, month, dayOfMonth) -> {
//                    Calendar selectedDate = Calendar.getInstance();
//                    selectedDate.set(year, month, dayOfMonth);
//                    listener.onDateSet(selectedDate.getTime());
//                },
//                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
//        );
//
//        // Eğer minDate varsa, bunu minimum tarih olarak ayarla
//        if (minDate != null) {
//            datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
//        }
//
//        datePickerDialog.show();
//    }

}