package com.example.android.inventory;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by mpombos on 2/7/16.
 */
public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        final InventoryDbHelper db = new InventoryDbHelper(this);
        Intent getListName = getIntent();
        final String productName = getListName.getExtras().getString("listItem");
        int pos = productName.indexOf("\nQuantity");
        final String subProductName = productName.substring(0, pos);
        Button decreaseQtyButton;

        final Cursor cur = db.getData(subProductName);

        if (cur.moveToFirst()) {

            // Set Product Name
            TextView tName = (TextView) findViewById(R.id.text_name);
            tName.setText(subProductName);

            // Set Price
            int price = cur.getInt(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));
            TextView tPrice = (TextView) findViewById(R.id.text_price);
            tPrice.setText("$" + price);

            // Set Quantity
            int quantity = cur.getInt(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
            TextView tQuantity = (TextView) findViewById(R.id.text_quantity);
            tQuantity.setText("" + quantity);
        }

        // Decrease quantity by 1
        decreaseQtyButton = (Button) findViewById(R.id.track);
        decreaseQtyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cur.moveToFirst()) {
                    int quantity = cur.getInt(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
                    if (quantity > 0) {
                        db.updateData(subProductName, quantity, -1);
                        quantity = cur.getInt(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
                        TextView tQuantity = (TextView) findViewById(R.id.text_quantity);
                        tQuantity.setText("" + quantity);
                        Toast.makeText(DetailsActivity.this, "Refresh!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(DetailsActivity.this, "It's empty! Order Now!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Increase quantity by 1
        Button btnReceive = (Button) findViewById(R.id.receive);
        btnReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cur.moveToFirst()) {
                    int quantity = cur.getInt(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
                    db.updateData(subProductName, quantity, 1);
                    quantity = cur.getInt(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
                    TextView tQuantity = (TextView) findViewById(R.id.text_quantity);
                    tQuantity.setText("" + quantity);
                    Toast.makeText(DetailsActivity.this, "Refresh!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Order Now
        Button orderNow = (Button) findViewById(R.id.order);
        if (orderNow != null) {
            orderNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:"))
                            .putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"})
                            .putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_request_for) + productName +"'")
                            .putExtra(Intent.EXTRA_TEXT
                                    , getString(R.string.dear) + "email@example.com"
                                            + getString(R.string.we_would_like_to_order_more_of_your_product) + productName
                                            + getString(R.string.regards));
                    if (intent.resolveActivity(getPackageManager()) != null){
                        startActivity(intent);
                    }
                }
            });
        }

        // delete row
        Button delete_ = (Button) findViewById(R.id.delete_data);
        delete_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (db.deleteData(subProductName)) {
                                    Intent returnHome = new Intent(DetailsActivity.this, MainActivity.class);
                                    startActivity(returnHome);
                                    Toast.makeText(DetailsActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(DetailsActivity.this);
                ab.setMessage("Are you sure you want to delete?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });

        // Convert byte array to bitmap and display the image
        ImageView img = (ImageView) findViewById(R.id.imageView);
        byte[] image = cur.getBlob(cur.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE));
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        img.setImageBitmap(bitmap);
    }
}
