package components.searchablespinnerlibrary;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import topgrade.parent.com.parentseeks.R;

public class SearchableListDialog<T extends Serializable> extends DialogFragment {

    private static final String ITEMS = "items";

    private ArrayAdapter<T> listAdapter;

    private ListView _listViewItems;

    private SearchableItem<T> _searchableItem;
    
    private String _strTitle;
    private String _strPositiveButtonText;
    private DialogInterface.OnClickListener _onClickListener;
    private OnSearchTextChanged _onSearchTextChanged;

    public SearchableListDialog() {

    }

    @NonNull
    public static <T extends Serializable> SearchableListDialog<T> newInstance(@Nullable List<T> items) {
        SearchableListDialog<T> multiSelectExpandableFragment = new
                SearchableListDialog<>();

        Bundle args = new Bundle();
        if (items != null) {
            args.putSerializable(ITEMS, new ArrayList<>(items));
        } else {
            args.putSerializable(ITEMS, new ArrayList<>());
        }

        multiSelectExpandableFragment.setArguments(args);

        return multiSelectExpandableFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_HIDDEN);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Getting the layout inflater to inflate the view in an alert dialog.
        LayoutInflater inflater = getLayoutInflater();

        // Restore searchable item instance on configuration change
        if (null != savedInstanceState) {
            //noinspection unchecked
            _searchableItem = (SearchableItem<T>) savedInstanceState.getSerializable("item");
        }

        View rootView = inflater.inflate(R.layout.searchable_list_dialog, null);
        setData(rootView);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
        alertDialog.setView(rootView);
        
        // Set title if provided
        if (_strTitle != null) {
            alertDialog.setTitle(_strTitle);
        }
        
        // Set positive button if provided
        if (_strPositiveButtonText != null) {
            if (_onClickListener != null) {
                alertDialog.setPositiveButton(_strPositiveButtonText, _onClickListener);
            } else {
                alertDialog.setPositiveButton(_strPositiveButtonText, (dialog1, which) -> {
                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                });
            }
        }

        final AlertDialog dialog = alertDialog.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_HIDDEN);
            
            // Set responsive dialog size - 85% of screen width
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (requireActivity().getResources().getDisplayMetrics().widthPixels * 0.85);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
            
            // Position the dialog more centered
            dialog.getWindow().setGravity(android.view.Gravity.CENTER);
            
            // Use rounded background drawable
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg_rounded);
        }
        
        // Post a runnable to ensure proper sizing after dialog is shown
        dialog.setOnShowListener(dialogInterface -> {
            // Recalculate height after dialog is shown
            calculateAndSetHeight();
        });
        
        return dialog;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable("item", _searchableItem);
        super.onSaveInstanceState(outState);
    }

    public void setOnSearchableItemClickListener(SearchableItem<T> searchableItem) {
        this._searchableItem = searchableItem;
    }

    /**
     * Updates the items in the dialog with new data
     * @param items New list of items to display
     */
    public void updateItems(@Nullable List<T> items) {
        Bundle args = getArguments();
        if (args == null) {
            args = new Bundle();
        }
        if (items != null) {
            args.putSerializable(ITEMS, new ArrayList<>(items));
        } else {
            args.putSerializable(ITEMS, new ArrayList<>());
        }
        setArguments(args);
        
        // Update adapter if already created
        if (listAdapter != null && items != null) {
            listAdapter.clear();
            listAdapter.addAll(items);
            listAdapter.notifyDataSetChanged();
            calculateAndSetHeight();
        }
    }

    private void setData(View rootView) {
        if (getArguments() == null) {
            return;
        }
        
        //noinspection unchecked
        List<T> items = (List<T>) getArguments().getSerializable(ITEMS);
        
        if (items == null) {
            return;
        }

        _listViewItems = rootView.findViewById(R.id.listItems);

        //create the adapter by passing your ArrayList data
        listAdapter = new ArrayAdapter<>(requireActivity(), R.layout.spinner_item_clean,
                items);
        //attach the adapter to the list
        _listViewItems.setAdapter(listAdapter);

        _listViewItems.setTextFilterEnabled(false);
        
        // Ensure no dividers or unwanted lines
        _listViewItems.setDivider(null);
        _listViewItems.setDividerHeight(0);
        
        // Calculate proper height based on number of items
        calculateAndSetHeight();

        _listViewItems.setOnItemClickListener((parent, view, position, id) -> {
            _searchableItem.onSearchableItemClicked(listAdapter.getItem(position), position);
            if (getDialog() != null) {
                getDialog().dismiss();
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Hide keyboard when dialog is dismissed
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getDialog() != null ? getDialog().getCurrentFocus() : null;
        if (view != null && imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    public interface SearchableItem<T> extends Serializable {
        void onSearchableItemClicked(T item, int position);
    }
    
    public interface OnSearchTextChanged {
        void onSearchTextChanged(String text);
    }
    
    /**
     * Sets the dialog title
     * @param title Title text
     */
    public void setTitle(String title) {
        this._strTitle = title;
    }
    
    /**
     * Sets the positive button text with default dismiss behavior
     * @param positiveButtonText Button text
     */
    public void setPositiveButton(String positiveButtonText) {
        this._strPositiveButtonText = positiveButtonText;
        this._onClickListener = null;
    }
    
    /**
     * Sets the positive button text with custom click listener
     * @param positiveButtonText Button text
     * @param onClickListener Click listener
     */
    public void setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener onClickListener) {
        this._strPositiveButtonText = positiveButtonText;
        this._onClickListener = onClickListener;
    }
    
    /**
     * Sets the search text changed listener
     * @param onSearchTextChanged Listener for search text changes
     */
    public void setOnSearchTextChangedListener(OnSearchTextChanged onSearchTextChanged) {
        this._onSearchTextChanged = onSearchTextChanged;
    }
    
    private void calculateAndSetHeight() {
        if (_listViewItems != null && listAdapter != null) {
            // Simplified and reliable height calculation
            _listViewItems.post(() -> {
                ViewGroup.LayoutParams params = _listViewItems.getLayoutParams();
                int maxItems = Math.min(listAdapter.getCount(), 6); // Max 6 items visible
                int itemHeight = (int) (48 * requireContext().getResources().getDisplayMetrics().density);
                params.height = maxItems * itemHeight;
                _listViewItems.setLayoutParams(params);
            });
        }
    }
}
