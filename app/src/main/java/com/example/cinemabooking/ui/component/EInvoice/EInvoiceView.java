package com.example.cinemabooking.ui.component.EInvoice;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.service.InvoiceService;
import com.example.cinemabookingapp.utils.DateTimeConverter;
import com.example.cinemabookingapp.utils.QRCodeGenerator;

import java.util.Locale;

public class EInvoiceView extends FrameLayout {
    public EInvoiceView(Context context, AttributeSet attrs){
        super(context, attrs);
        inflate(context, R.layout.invoice_view_layout, this);
        initViews();
    }

    TextView invoiceIdTV, createDateTimeTV, movieNameTV, movieGernesTV, cinemaNameTV, showtimeTV,
            bookingSeatsTV, seatsPriceTV, snackTotalPriceTV, discountTV, totalPriceTV, paymentMethodTV, transactionIdTV;
    RecyclerView snackListView;
    ImageView invoiceIdQR, movieBannerImage;

    void initViews(){
        invoiceIdTV = findViewById(R.id.invoice_number);
        createDateTimeTV = findViewById(R.id.invoice_create_datetime);
        movieNameTV = findViewById(R.id.invoice_movie_name);
        movieGernesTV = findViewById(R.id.invoice_movie_gernes);
        cinemaNameTV = findViewById(R.id.invoice_cinema_name);
        showtimeTV = findViewById(R.id.invoice_showtime_datetime);
        bookingSeatsTV = findViewById(R.id.invoice_booking_seats);
        seatsPriceTV = findViewById(R.id.invoice_seats_price);
        snackTotalPriceTV = findViewById(R.id.invoice_snacks_total_price);
        discountTV = findViewById(R.id.invoice_price_discount);
        totalPriceTV = findViewById(R.id.invoice_price_total);
        paymentMethodTV = findViewById(R.id.invoice_payment_method);
        transactionIdTV = findViewById(R.id.invoice_transaction_id);

        snackListView = findViewById(R.id.invoice_snacks_listview);

        invoiceIdQR = findViewById(R.id.invoice_qr_bitmap);
        movieBannerImage = findViewById(R.id.invoice_movie_banner);
    }

    void bindViewData(InvoiceService.InvoiceDetail invoiceDetail){

        invoiceIdTV.setText(invoiceDetail.booking.bookingId);
        try {
            invoiceIdQR.setImageBitmap(QRCodeGenerator.generateQRCodeFromString(invoiceDetail.booking.bookingId));
        }catch (Exception ignored){ }

        invoiceIdTV.setText(invoiceDetail.booking.bookingId);
        createDateTimeTV.setText(DateTimeConverter.convertToDateTimeString(invoiceDetail.booking.createdAt));
        
        movieNameTV.setText(invoiceDetail.booking.movieTitleSnapshot);
        movieGernesTV.setText(String.join(", ", invoiceDetail.movie.genres));
        Glide.with(this)
                .load(invoiceDetail.movie.posterUrl)
                .into(movieBannerImage);

//        cinemaNameTV.setText();
        showtimeTV.setText(DateTimeConverter.convertToDateTimeString(invoiceDetail.showtime.startAt));
        bookingSeatsTV.setText(String.join(",", invoiceDetail.booking.seatCodes));

        seatsPriceTV.setText(String.format(Locale.ENGLISH, "%,dvnd", (int) invoiceDetail.booking.subtotal));
//        snackTotalPriceTV.setText();
        discountTV.setText(String.format("%,dvnd",(int) invoiceDetail.booking.discount));
        totalPriceTV.setText(String.format("%,dvnd",(int) invoiceDetail.booking.total));
        discountTV.setText(String.format("%,dvnd",(int) invoiceDetail.booking.discount));

        paymentMethodTV.setText(invoiceDetail.booking.paymentMethod);
//        transactionIdTV.setText(invoiceDetail.booking);

    }

    InvoiceService.InvoiceDetail mBooking;
    public void setInvoiceDetail(InvoiceService.InvoiceDetail invoiceDetail){
        mBooking = invoiceDetail;
        bindViewData(invoiceDetail);
    }
}
