package net.senekal.openchrono;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/*
 * e.g: ErrorDialog.show(this, "Debug application, do not trade!!!NEW ");
 */
public class ErrorDialog {

    public static void show(Context context, String errorMessage) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.error_dialog, null);
        TextView errorTextView = dialogView.findViewById(R.id.error_message);
        Button closeButton = dialogView.findViewById(R.id.close_button);

        // Set the error message
        errorTextView.setText(errorMessage);

        // Create the AlertDialog
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Set the close button click listener
        closeButton.setOnClickListener(v -> alertDialog.dismiss());

        // Show the dialog
        alertDialog.show();
    }
}