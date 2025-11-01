# 🎨 UI/UX Quick Reference - Topgradeit

## 🚀 Quick Start

### Add Animation to Card
```kotlin
import topgrade.parent.com.parentseeks.Parent.Utils.UIEnhancementHelper

// Fade in
UIEnhancementHelper.fadeIn(cardView)

// Scale in with bounce
UIEnhancementHelper.scaleIn(cardView)

// Staggered animation for multiple cards
UIEnhancementHelper.staggeredFadeIn(listOf(card1, card2, card3))
```

### Make View Accessible
```kotlin
import topgrade.parent.com.parentseeks.Parent.Utils.AccessibilityHelper

// Setup card accessibility
AccessibilityHelper.setupCardAccessibility(
    card = dashboardCard,
    title = "Dashboard",
    description = "View your information"
)

// Ensure touch target size
AccessibilityHelper.ensureTouchTargetSize(button)
```

### Add Loading State
```xml
<!-- In your layout -->
<include layout="@layout/layout_modern_loading_state"
    android:id="@+id/loading_state" />
```

```kotlin
// Show loading
UIEnhancementHelper.fadeIn(findViewById(R.id.loading_state))

// Hide loading
UIEnhancementHelper.fadeOut(findViewById(R.id.loading_state))
```

---

## 📖 Common Patterns

### Pattern 1: Animated List Entry
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val items = listOf(item1, item2, item3)
    UIEnhancementHelper.staggeredFadeIn(items, delayBetween = 80L)
}
```

### Pattern 2: Button with Press Effect
```kotlin
button.setOnClickListener {
    UIEnhancementHelper.addPressEffect(it)
    // Your click handling
}
```

### Pattern 3: Error Shake
```kotlin
fun showError(view: View) {
    UIEnhancementHelper.shake(view)
    AccessibilityHelper.announce(view, "Error: Invalid input")
}
```

### Pattern 4: Loading → Content Transition
```kotlin
fun loadData() {
    UIEnhancementHelper.fadeIn(loadingView)
    UIEnhancementHelper.fadeOut(contentView)
    
    viewModel.fetchData { data ->
        UIEnhancementHelper.fadeOut(loadingView)
        UIEnhancementHelper.fadeIn(contentView)
    }
}
```

---

## 🎯 Animation Reference

| Method | Duration | Use Case |
|--------|----------|----------|
| `fadeIn()` | 300ms | Show views smoothly |
| `fadeOut()` | 200ms | Hide views smoothly |
| `slideUpFadeIn()` | 300ms | Bottom sheet, cards |
| `scaleIn()` | 300ms | Dialog, modal entry |
| `shake()` | 500ms | Error indication |
| `pulse()` | 1000ms | Draw attention |
| `rotate()` | 300ms | Refresh icon |
| `staggeredFadeIn()` | 50ms delay | List animations |

---

## ♿ Accessibility Reference

| Method | Purpose |
|--------|---------|
| `setupCardAccessibility()` | Cards, tiles |
| `setupButtonAccessibility()` | All buttons |
| `setupFormFieldAccessibility()` | Input fields |
| `setupListItemAccessibility()` | List items |
| `ensureTouchTargetSize()` | 48dp minimum |
| `announce()` | Screen reader announcements |
| `setAsHeading()` | Section titles |

---

## 🎨 Gradient Backgrounds

```xml
<!-- Parent -->
<MaterialCardView
    android:background="@drawable/bg_card_gradient_parent">

<!-- Student -->
<MaterialCardView
    android:background="@drawable/bg_card_gradient_student">

<!-- Staff -->
<MaterialCardView
    android:background="@drawable/bg_card_gradient_staff">
```

---

## 🔧 Useful Snippets

### Ripple Effect
```xml
<View
    android:foreground="@drawable/ripple_effect_white"
    android:clickable="true" />
```

### Circular Reveal (API 21+)
```kotlin
UIEnhancementHelper.circularReveal(view, centerX, centerY)
```

### Cross-fade Between Views
```kotlin
UIEnhancementHelper.crossFade(oldView, newView)
```

### Elevate Card on Touch
```kotlin
card.setOnTouchListener { v, event ->
    when (event.action) {
        MotionEvent.ACTION_DOWN -> 
            UIEnhancementHelper.elevateCard(v as MaterialCardView)
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> 
            UIEnhancementHelper.resetCardElevation(v as MaterialCardView)
    }
    false
}
```

---

## 📱 Responsive Dimensions

Use `@dimen/_XXsdp` for size, `@dimen/_XXssp` for text:

```xml
<!-- Size -->
android:layout_width="@dimen/_48sdp"

<!-- Text -->
android:textSize="@dimen/_16ssp"
```

---

## ⚡ Performance Tips

1. **Reuse views** - Use ViewHolder pattern
2. **Cancel animations** - In `onDestroy()`
3. **Hardware acceleration** - Enabled by default
4. **Lazy loading** - Load data on demand
5. **Image optimization** - Use Glide with proper sizing

---

## 🐛 Debugging

```kotlin
// Check if accessibility is enabled
if (AccessibilityHelper.isAccessibilityEnabled(context)) {
    Log.d("A11y", "TalkBack is ON")
}

// Check animation duration
Log.d("Animation", "Duration: ${UIEnhancementHelper.DEFAULT_DURATION}ms")
```

---

## 📚 Full Documentation

See `UI_UX_ENHANCEMENT_GUIDE.md` for complete documentation.

---

**Quick Reference v1.0** | Last Updated: Oct 15, 2025

