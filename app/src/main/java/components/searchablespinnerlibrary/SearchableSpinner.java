package components.searchablespinnerlibrary;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import topgrade.parent.com.parentseeks.R;

public class SearchableSpinner extends androidx.appcompat.widget.AppCompatSpinner implements View.OnTouchListener,
        SearchableListDialog.SearchableItem<Serializable> {

    public static final int NO_ITEM_SELECTED = -1;
    private final Context _context;
    private List<Serializable> _items;
    private SearchableListDialog<Serializable> _searchableListDialog;

    private boolean _isDirty;
    private ArrayAdapter<Serializable> _arrayAdapter;
    private String _strHintText;
    private boolean _isFromInit;
    private OnItemSelectedListener _externalListener;

    public SearchableSpinner(Context context) {
        super(context);
        this._context = context;
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchableSpinner)) {
            final int N = a.getIndexCount();
            for (int i = 0; i < N; ++i) {
                int attr = a.getIndex(i);
                if (attr == R.styleable.SearchableSpinner_hintText) {
                    _strHintText = a.getString(attr);
                }
            }
        }
        init();
    }

    public SearchableSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
    }

    private void init() {
        _items = new ArrayList<>();
        _searchableListDialog = SearchableListDialog.newInstance(_items);
        _searchableListDialog.setOnSearchableItemClickListener(this);
        setOnTouchListener(this);

        // Safe adapter casting with type checking
        SpinnerAdapter adapter = getAdapter();
        if (adapter instanceof ArrayAdapter<?>) {
            //noinspection unchecked
            _arrayAdapter = (ArrayAdapter<Serializable>) adapter;
        } else {
            _arrayAdapter = null;
        }
        
        // Only set hint adapter if hint text exists
        if (!TextUtils.isEmpty(_strHintText)) {
            ArrayAdapter<String> hintAdapter = new ArrayAdapter<>(
                _context, 
                R.layout.custom_spinner_item, 
                new String[]{_strHintText}
            );
            _isFromInit = true;
            setAdapter(hintAdapter);
            applyHintStyling();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (_searchableListDialog != null && !_searchableListDialog.isAdded()) {
                refreshItems();
                showDialog();
            }
        }
        return true; // Consume the touch event to prevent native Spinner dropdown
    }
    
    /**
     * Refreshes the items list from the current adapter
     */
    private void refreshItems() {
        if (_arrayAdapter != null) {
            _items.clear();
            for (int i = 0; i < _arrayAdapter.getCount(); i++) {
                _items.add(_arrayAdapter.getItem(i));
            }
            _searchableListDialog.updateItems(_items);
        }
    }
    
    /**
     * Shows the searchable dialog with proper lifecycle checks
     */
    private void showDialog() {
        Activity activity = scanForActivity(_context);
        if (activity instanceof androidx.appcompat.app.AppCompatActivity appCompatActivity) {
            // Check activity and fragment state to prevent crashes
            if (!appCompatActivity.isFinishing() && 
                !appCompatActivity.getSupportFragmentManager().isStateSaved()) {
                // Use meaningful tag for debugging and multiple spinner support
                String tag = getClass().getSimpleName() + "_" + getId();
                _searchableListDialog.show(appCompatActivity.getSupportFragmentManager(), tag);
            }
        }
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {
        if (!_isFromInit) {
            // Safe adapter casting with type checking
            if (adapter instanceof ArrayAdapter<?>) {
                //noinspection unchecked
                _arrayAdapter = (ArrayAdapter<Serializable>) adapter;
            } else {
                _arrayAdapter = null;
            }
            
            if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
                ArrayAdapter<String> hintAdapter = new ArrayAdapter<>(
                    _context, 
                    R.layout.custom_spinner_item, 
                    new String[]{_strHintText}
                );
                super.setAdapter(hintAdapter);
                applyHintStyling();
            } else {
                super.setAdapter(adapter);
            }
        } else {
            _isFromInit = false;
            super.setAdapter(adapter);
        }
    }

    @Override
    public void onSearchableItemClicked(Serializable item, int position) {
        int itemIndex = _items.indexOf(item);
        setSelection(itemIndex);

        if (!_isDirty) {
            _isDirty = true;
            setAdapter(_arrayAdapter);
            setSelection(itemIndex);
            
            // Restore normal text color after selection
            post(() -> {
                try {
                    View selectedView = getSelectedView();
                    if (selectedView instanceof TextView textView) {
                        textView.setTextColor(ContextCompat.getColor(_context, R.color.black));
                    }
                } catch (Exception e) {
                    // Silently handle if view is not ready
                }
            });
        }
        
        // Notify external listener if set
        if (_externalListener != null) {
            _externalListener.onItemSelected(this, getSelectedView(), itemIndex, getSelectedItemId());
        }
    }

    @Override
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this._externalListener = listener;
        super.setOnItemSelectedListener(listener);
    }

    public void setTitle(String strTitle) {
        _searchableListDialog.setTitle(strTitle);
    }

    public void setPositiveButton(String strPositiveButtonText) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText);
    }

    public void setPositiveButton(String strPositiveButtonText, DialogInterface.OnClickListener onClickListener) {
        _searchableListDialog.setPositiveButton(strPositiveButtonText, onClickListener);
    }

    @SuppressWarnings("unused")
    public void setOnSearchTextChangedListener(SearchableListDialog.OnSearchTextChanged onSearchTextChanged) {
        _searchableListDialog.setOnSearchTextChangedListener(onSearchTextChanged);
    }

    /**
     * Sets the items for the spinner programmatically
     * @param items List of items to display (must be Serializable)
     */
    public void setItems(List<? extends Serializable> items) {
        _items.clear();
        if (items != null) {
            _items.addAll(items);
        }
        _isDirty = false;
        // Create adapter with the items
        _arrayAdapter = new ArrayAdapter<>(
            _context, 
            R.layout.custom_spinner_item, 
            _items
        );
        setAdapter(_arrayAdapter);
    }

    /**
     * Sets the hint text for the spinner
     * @param hint Hint text to display when no item is selected
     */
    public void setHint(String hint) {
        this._strHintText = hint;
        // Lightweight update without full reinitialization
        if (!_isDirty && !TextUtils.isEmpty(_strHintText)) {
            resetToHint();
        }
    }

    /**
     * Gets the current hint text
     * @return Current hint text
     */
    public String getHint() {
        return _strHintText;
    }

    /**
     * Checks if user has made a selection (not just showing hint)
     * @return true if user selected an item, false if still showing hint
     */
    @SuppressWarnings("unused")
    public boolean hasUserSelection() {
        return _isDirty;
    }

    /**
     * Resets the spinner to show the hint (if hint is set)
     */
    public void resetToHint() {
        if (!TextUtils.isEmpty(_strHintText)) {
            _isDirty = false;
            ArrayAdapter<String> hintAdapter = new ArrayAdapter<>(
                _context, 
                R.layout.custom_spinner_item, 
                new String[]{_strHintText}
            );
            _isFromInit = true;
            setAdapter(hintAdapter);
            
            // Apply hint styling (gray color) for visual feedback
            applyHintStyling();
        }
    }
    
    /**
     * Applies gray styling to hint text for better UX
     */
    private void applyHintStyling() {
        post(() -> {
            try {
                View selectedView = getSelectedView();
                if (selectedView instanceof TextView textView && !_isDirty) {
                    // Use gray color for hint text
                    textView.setTextColor(Color.parseColor("#999999"));
                }
            } catch (Exception e) {
                // Silently handle if view is not ready
            }
        });
    }

    private Activity scanForActivity(Context cont) {
        if (cont == null)
            return null;
        else if (cont instanceof Activity)
            return (Activity) cont;
        else if (cont instanceof ContextWrapper)
            return scanForActivity(((ContextWrapper) cont).getBaseContext());

        return null;
    }

    @Override
    public int getSelectedItemPosition() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return NO_ITEM_SELECTED;
        } else {
            return super.getSelectedItemPosition();
        }
    }

    @Override
    public Object getSelectedItem() {
        if (!TextUtils.isEmpty(_strHintText) && !_isDirty) {
            return null;
        } else {
            return super.getSelectedItem();
        }
    }
}
