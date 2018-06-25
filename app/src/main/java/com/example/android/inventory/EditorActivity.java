
package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private int mQuantity;
    private EditText productNameEdit;
    private EditText priceEdit;
    private EditText quantityEdit;
    private EditText supplierNameEdit;
    private EditText supplierNumberEdit;
    private ImageButton add;
    private ImageButton sub;
    private Uri mCurrentProductUri;
    private Button mOrder;
    private Button mdelete;
    private boolean hasAllRequiredValues = false;
    private static final int EXISTING_PRODUCT_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mOrder = (Button) findViewById(R.id.order);
        mdelete = (Button) findViewById(R.id.delete);
        productNameEdit = (EditText) findViewById(R.id.product_name);
        priceEdit = (EditText) findViewById(R.id.price);
        quantityEdit = (EditText) findViewById(R.id.quantity);
        supplierNameEdit = (EditText) findViewById(R.id.supplier_name);
        supplierNumberEdit = (EditText) findViewById(R.id.supplier_number);
        add = (ImageButton) findViewById(R.id.add);
        sub = (ImageButton) findViewById(R.id.sub);

        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (quantityEdit.getText().toString().equals("") || quantityEdit.getText().toString() == null) {
                    quantityEdit.setText("0");
                } else {
                    int a = Integer.parseInt(quantityEdit.getText().toString());
                    int b = a + 1;
                    quantityEdit.setText(String.valueOf(b));
                }

            }
        });

        sub.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                int a = Integer.parseInt(quantityEdit.getText().toString());
                if (a >= 1) {
                    int b = a - 1;

                    quantityEdit.setText(String.valueOf(b));
                } else {

                    quantityEdit.setText("0");
                }
            }
        });

        mOrder.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                String number = supplierNumberEdit.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                startActivity(intent);
            }
        });

        mdelete.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                deleteDialog();
            }
        });

        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle("add an item");
            productNameEdit.setEnabled(true);
            priceEdit.setEnabled(true);
            quantityEdit.setEnabled(true);
            supplierNameEdit.setEnabled(true);
            supplierNumberEdit.setEnabled(true);
            mOrder.setVisibility(View.GONE);
            mdelete.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            setTitle("Edit product");
            productNameEdit.setEnabled(false);
            priceEdit.setEnabled(false);
            quantityEdit.setEnabled(false);
            supplierNameEdit.setEnabled(false);
            supplierNumberEdit.setEnabled(false);

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {

        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.delete_error),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.delete_success),
                        Toast.LENGTH_SHORT).show();
            }
        }


        finish();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private boolean saveProduct() {

        int quantity;
        String nameString = productNameEdit.getText().toString().trim();
        String quantityString = quantityEdit.getText().toString().trim();
        String priceString = priceEdit.getText().toString().trim();
        String supplierNameString = supplierNameEdit.getText().toString().trim();
        String supplierNumberString = supplierNumberEdit.getText().toString().trim();

        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierNumberString)
                ) {

            hasAllRequiredValues = true;
            return hasAllRequiredValues;
        }

        ContentValues values = new ContentValues();


        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_name), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_quantity), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            // If the quantity is not provided by the user, don't try to parse the string into an
            // integer value. Use 0 by default.
            quantity = Integer.parseInt(quantityString);
            values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_price), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_supplier_name), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierNameString);
        }
        if (TextUtils.isEmpty(supplierNumberString)) {
            Toast.makeText(this, getString(R.string.validation_msg_product_supplier_number), Toast.LENGTH_SHORT).show();
            return hasAllRequiredValues;
        } else {
            values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER, supplierNumberString);
        }

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }

        hasAllRequiredValues = true;
        return hasAllRequiredValues;
    }

    private void editing() {
        setTitle("Editing ..");
        productNameEdit.setEnabled(true);
        priceEdit.setEnabled(true);
        quantityEdit.setEnabled(true);
        supplierNameEdit.setEnabled(true);
        supplierNumberEdit.setEnabled(true);
        mOrder.setVisibility(View.GONE);
        mdelete.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                if (hasAllRequiredValues == true) {
                    finish();
                }
                return true;
            case R.id.action_edit:
                editing();
                closeOptionsMenu();
                return true;
            case R.id.action_delete:
                deleteDialog();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY
        };

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierNumberColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierNumber = cursor.getString(supplierNumberColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            mQuantity = quantity;

            // Update the views on the screen with the values from the database
            productNameEdit.setText(name);
            priceEdit.setText(price);
            supplierNameEdit.setText(supplierName);
            supplierNumberEdit.setText(supplierNumber);
            quantityEdit.setText(Integer.toString(quantity));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameEdit.setText("");
        priceEdit.setText("");
        supplierNameEdit.setText("");
        supplierNumberEdit.setText("");
        quantityEdit.setText("0");


    }
}