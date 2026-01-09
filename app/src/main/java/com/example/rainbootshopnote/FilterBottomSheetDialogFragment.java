package com.example.rainbootshopnote;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.rainbootshopnote.utils.date.AfPersianDateUtils;
import com.example.rainbootshopnote.utils.date.DateNormalize;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnFilterAppliedListener {
        void onFilterApplied(@Nullable String fromDate, @Nullable String toDate, int flowIndex, boolean showSolar);
        void onFilterCleared();
    }

    private static final String ARG_FROM = "arg_from";              // Gregorian yyyy-MM-dd OR null
    private static final String ARG_TO = "arg_to";                  // Gregorian yyyy-MM-dd OR null
    private static final String ARG_OPTION = "arg_option";
    private static final String ARG_SHOW_SPINNER = "arg_show_spinner";
    private static final String ARG_LABELS = "arg_labels";
    private static final String ARG_SHOW_SOLAR = "arg_show_solar";  // boolean

    private OnFilterAppliedListener listener;

    // Internal selection stored as Afghan numeric (1404-04-14) for UI rendering
    @Nullable private String selectedFromAf;
    @Nullable private String selectedToAf;

    // Sheet içinde anlık calendar mode
    private boolean currentShowSolar = false; // false=میلادی, true=هجری شمسی

    public static FilterBottomSheetDialogFragment newInstance(
            @Nullable String fromDateGreg,   // yyyy-MM-dd
            @Nullable String toDateGreg,     // yyyy-MM-dd
            int optionIndex,
            boolean showSpinner,
            @Nullable String[] spinnerLabels,
            boolean showSolar
    ) {
        FilterBottomSheetDialogFragment f = new FilterBottomSheetDialogFragment();
        Bundle b = new Bundle();
        b.putString(ARG_FROM, fromDateGreg);
        b.putString(ARG_TO, toDateGreg);
        b.putInt(ARG_OPTION, optionIndex);
        b.putBoolean(ARG_SHOW_SPINNER, showSpinner);
        b.putStringArray(ARG_LABELS, spinnerLabels);
        b.putBoolean(ARG_SHOW_SOLAR, showSolar);
        f.setArguments(b);
        return f;
    }

    public void setOnFilterAppliedListener(@Nullable OnFilterAppliedListener l) {
        this.listener = l;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        View rowFrom = view.findViewById(R.id.rowFromDate);
        View rowTo = view.findViewById(R.id.rowToDate);

        TextView txtFromValue = view.findViewById(R.id.txtFromValue);
        TextView txtFromGreg = view.findViewById(R.id.txtFromValueGreg);

        TextView txtToValue = view.findViewById(R.id.txtToValue);
        TextView txtToGreg = view.findViewById(R.id.txtToValueGreg);

        TextView txtFlowLabel = view.findViewById(R.id.txtFlowTypeLabel);
        View rowFlowContainer = view.findViewById(R.id.rowFlowType);
        Spinner spFlow = view.findViewById(R.id.spFlowType);

        Spinner spCalendarMode = view.findViewById(R.id.spCalendarMode);

        Button btnApply = view.findViewById(R.id.btnFilterApply);
        Button btnClear = view.findViewById(R.id.btnFilterClear);

        Bundle args = getArguments();
        boolean showSpinner = args == null || args.getBoolean(ARG_SHOW_SPINNER, true);
        int optionIndex = args != null ? args.getInt(ARG_OPTION, 0) : 0;
        String[] labels = args != null ? args.getStringArray(ARG_LABELS) : null;

        // Incoming values are expected Gregorian yyyy-MM-dd
        String fromGregArg = args != null ? args.getString(ARG_FROM) : null;
        String toGregArg = args != null ? args.getString(ARG_TO) : null;

        // Convert incoming Gregorian -> Afghan numeric for internal state
        selectedFromAf = gregToAfNumericSafe(fromGregArg);
        selectedToAf = gregToAfNumericSafe(toGregArg);

        // Flow spinner visible/hidden
        if (!showSpinner) {
            txtFlowLabel.setVisibility(View.GONE);
            rowFlowContainer.setVisibility(View.GONE);
        } else {
            txtFlowLabel.setVisibility(View.VISIBLE);
            rowFlowContainer.setVisibility(View.VISIBLE);

            if (labels == null || labels.length == 0) {
                labels = new String[]{"همه", "بقایات", "رسیده ها"};
            }

            ArrayAdapter<String> ad = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    labels
            );
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spFlow.setAdapter(ad);

            if (optionIndex < 0) optionIndex = 0;
            if (optionIndex >= labels.length) optionIndex = 0;
            spFlow.setSelection(optionIndex);
        }

        // Calendar mode spinner
        String[] calLabels = new String[]{"میلادی", "هجری شمسی"};
        ArrayAdapter<String> calAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                calLabels
        );
        calAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCalendarMode.setAdapter(calAdapter);

        currentShowSolar = args != null && args.getBoolean(ARG_SHOW_SOLAR, false);
        spCalendarMode.setSelection(currentShowSolar ? 1 : 0);

        // First render (based on currentShowSolar)
        render(txtFromValue, txtFromGreg, selectedFromAf, currentShowSolar);
        render(txtToValue, txtToGreg, selectedToAf, currentShowSolar);

        // Change mode => update UI instantly
        spCalendarMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                currentShowSolar = (position == 1); // 0=میلادی, 1=هجری شمسی
                render(txtFromValue, txtFromGreg, selectedFromAf, currentShowSolar);
                render(txtToValue, txtToGreg, selectedToAf, currentShowSolar);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Pick FROM date (Gregorian picker, then convert to Afghan numeric)
        rowFrom.setOnClickListener(x -> {
            Calendar initial = Calendar.getInstance();
            if (selectedFromAf != null) {
                Calendar g = AfPersianDateUtils.afNumericToGregCalendar(selectedFromAf);
                if (g != null) initial = g;
            }
            showGregorianPicker(initial, null, pickedGreg -> {
                AfPersianDateUtils.AfDate af = AfPersianDateUtils.gregToAf(pickedGreg);
                if (af == null) return;

                selectedFromAf = AfPersianDateUtils.formatAfNumeric(af);

                // If TO < FROM => clear TO
                if (selectedToAf != null && compareAfNumeric(selectedToAf, selectedFromAf) < 0) {
                    selectedToAf = null;
                }

                render(txtFromValue, txtFromGreg, selectedFromAf, currentShowSolar);
                render(txtToValue, txtToGreg, selectedToAf, currentShowSolar);
            });
        });

        // Pick TO date (must have FROM)
        rowTo.setOnClickListener(x -> {
            if (selectedFromAf == null) {
                toastFa("ابتدا تاریخ شروع را انتخاب کنید.");
                return;
            }

            Calendar min = AfPersianDateUtils.afNumericToGregCalendar(selectedFromAf);
            Calendar initial = Calendar.getInstance();

            if (selectedToAf != null) {
                Calendar g = AfPersianDateUtils.afNumericToGregCalendar(selectedToAf);
                if (g != null) initial = g;
            } else if (min != null) {
                initial = (Calendar) min.clone();
            }

            showGregorianPicker(initial, min, pickedGreg -> {
                AfPersianDateUtils.AfDate af = AfPersianDateUtils.gregToAf(pickedGreg);
                if (af == null) return;

                String candidate = AfPersianDateUtils.formatAfNumeric(af);
                if (compareAfNumeric(candidate, selectedFromAf) < 0) {
                    toastFa("تاریخ ختم باید بعد از تاریخ شروع باشد.");
                    return;
                }

                selectedToAf = candidate;
                render(txtToValue, txtToGreg, selectedToAf, currentShowSolar);
            });
        });

        // Apply (RETURN GREGORIAN: yyyy-MM-dd, Latin)
        btnApply.setOnClickListener(v -> {
            int flowIndex = (spFlow.getVisibility() == View.VISIBLE)
                    ? spFlow.getSelectedItemPosition()
                    : 0;

            // Af numeric -> Gregorian
            String fromGreg = afNumericToGregSafe(selectedFromAf);
            String toGreg = afNumericToGregSafe(selectedToAf);

            // Normalize to yyyy-MM-dd (Latin)
            fromGreg = DateNormalize.extractIsoDay(fromGreg);
            toGreg = DateNormalize.extractIsoDay(toGreg);

            // Rule: if to selected, from is required
            if (toGreg != null && fromGreg == null) {
                toastFa("ابتدا تاریخ شروع را انتخاب کنید.");
                return;
            }

            // Rule: if both exist, to >= from
            if (fromGreg != null && toGreg != null) {
                Date fromD = parseIsoDayToDate(fromGreg);
                Date toD = parseIsoDayToDate(toGreg);
                if (fromD != null && toD != null && toD.before(fromD)) {
                    toastFa("تاریخ ختم باید بعد از تاریخ شروع باشد.");
                    return;
                }
            }

            if (listener != null) {
                // ✅ IMPORTANT: send currentShowSolar (not args value)
                listener.onFilterApplied(fromGreg, toGreg, flowIndex, currentShowSolar);
            }

            dismiss();
        });

        // Clear
        btnClear.setOnClickListener(v -> {
            selectedFromAf = null;
            selectedToAf = null;

            render(txtFromValue, txtFromGreg, null, currentShowSolar);
            render(txtToValue, txtToGreg, null, currentShowSolar);

            if (spFlow.getVisibility() == View.VISIBLE) spFlow.setSelection(0);

            if (listener != null) listener.onFilterCleared();

            dismiss();
        });

        return view;
    }

    /**
     * UI render: mode'a göre primary/secondary değişir.
     * - currentShowSolar=true: primary Solar (month-name), secondary Gregorian
     * - currentShowSolar=false: primary Gregorian, secondary Solar (month-name)
     */
    private void render(@NonNull TextView primaryLine,
                        @NonNull TextView secondaryLine,
                        @Nullable String afNumeric,
                        boolean currentShowSolar) {

        if (afNumeric == null) {
            primaryLine.setText("—");
            secondaryLine.setVisibility(View.GONE);
            secondaryLine.setText("");
            return;
        }

        AfPersianDateUtils.AfDate af = AfPersianDateUtils.parseAf(afNumeric);

        String solarText = (af != null)
                ? AfPersianDateUtils.formatAfWithMonthName(af)
                : afNumeric;

        String gregText = (af != null) ? AfPersianDateUtils.afToGreg(af) : null;

        if (currentShowSolar) {
            primaryLine.setText(solarText);

            if (gregText != null && !gregText.isEmpty()) {
                secondaryLine.setText(gregText);
                secondaryLine.setVisibility(View.VISIBLE);
            } else {
                secondaryLine.setVisibility(View.GONE);
                secondaryLine.setText("");
            }
        } else {
            if (gregText != null && !gregText.isEmpty()) {
                primaryLine.setText(gregText);
            } else {
                primaryLine.setText("—");
            }

            secondaryLine.setText(solarText);
            secondaryLine.setVisibility(View.VISIBLE);
        }
    }

    private void showGregorianPicker(@NonNull Calendar initial,
                                     @Nullable Calendar minDate,
                                     @NonNull OnGregPicked cb) {

        int y = initial.get(Calendar.YEAR);
        int m = initial.get(Calendar.MONTH);
        int d = initial.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dlg = new DatePickerDialog(
                requireContext(),
                (pickerView, year, month, dayOfMonth) -> {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth, 0, 0, 0);
                    c.set(Calendar.MILLISECOND, 0);

                    String picked = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    cb.onPicked(picked);
                },
                y, m, d
        );

        if (minDate != null) {
            dlg.getDatePicker().setMinDate(minDate.getTimeInMillis());
        }
        dlg.show();
    }

    @Nullable
    private Date parseIsoDayToDate(@Nullable String isoDay) {
        try {
            if (isoDay == null) return null;
            return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDay);
        } catch (Exception e) {
            return null;
        }
    }

    private interface OnGregPicked {
        void onPicked(@NonNull String gregYyyyMmDd);
    }

    private void toastFa(@NonNull String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Compare two Afghan numeric dates by converting them to Gregorian Calendar
     * Returns: <0 if A < B, 0 equal/unknown, >0 if A > B
     */
    private int compareAfNumeric(@NonNull String afA, @NonNull String afB) {
        Calendar ga = AfPersianDateUtils.afNumericToGregCalendar(afA);
        Calendar gb = AfPersianDateUtils.afNumericToGregCalendar(afB);
        if (ga == null || gb == null) return 0;
        return Long.compare(ga.getTimeInMillis(), gb.getTimeInMillis());
    }

    @Nullable
    private String afNumericToGregSafe(@Nullable String afNumeric) {
        AfPersianDateUtils.AfDate af = AfPersianDateUtils.parseAf(afNumeric);
        if (af == null) return null;
        return AfPersianDateUtils.afToGreg(af); // yyyy-MM-dd
    }

    @Nullable
    private String gregToAfNumericSafe(@Nullable String gregYyyyMmDd) {
        AfPersianDateUtils.AfDate af = AfPersianDateUtils.gregToAf(gregYyyyMmDd);
        if (af == null) return null;
        return AfPersianDateUtils.formatAfNumeric(af);
    }
}
