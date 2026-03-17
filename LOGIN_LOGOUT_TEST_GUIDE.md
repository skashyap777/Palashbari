# 🧪 Login & Logout Testing Guide

## ✅ What's Ready to Test

### **1. Login Flow**
- Real API integration with `https://palasbari.h24x7.in/api/login`
- Secure token storage
- Auto-navigation on success

### **2. Logout Flow**
- Real API call to `/logout` endpoint
- Clears all stored tokens and user data
- Returns to login screen

---

## 📝 Testing Steps

### **Test 1: Fresh Login**

1. **App should open on Login Screen** (if not logged in)
2. **Credentials are pre-filled:**
   - Username: `Palashbari_Dev`
   - Password: `Palashbari987@.!`
3. **Tap "Signin" button**
4. **Expected behavior:**
   - ✅ Button shows loading spinner
   - ✅ After ~1-2 seconds, navigates to Home Screen
   - ✅ Home screen shows dashboard with 24 icons

### **Test 2: Logout**

1. **From Home Screen, tap Profile icon** (top-right)
2. **You'll see Settings Screen with:**
   - Profile info at top
   - Menu items (Edit Profile, Change Password, etc.)
   - **"Logout"** in red at the bottom
3. **Tap "Logout"**
4. **Expected behavior:**
   - ✅ Shows confirmation dialog: "Are you sure you want to logout?"
   - ✅ Tap "Logout" button
   - ✅ Shows loading spinner
   - ✅ After ~1 second, returns to Login Screen
   - ✅ All tokens cleared from storage

### **Test 3: Re-login After Logout**

1. **Should be on Login Screen**
2. **Credentials still pre-filled**
3. **Tap "Signin" again**
4. **Expected behavior:**
   - ✅ Logs in successfully
   - ✅ Returns to Home Screen
   - ✅ New access token saved

### **Test 4: Auto-Login (App Restart)**

1. **After successful login, close the app completely**
2. **Reopen the app**
3. **Expected behavior:**
   - ✅ Shows loading spinner briefly
   - ✅ Automatically goes to Home Screen (skips login)
   - ✅ Uses saved token from previous session

### **Test 5: Error Handling**

1. **On Login Screen, change password to wrong value**
2. **Tap "Signin"**
3. **Expected behavior:**
   - ✅ Shows red error message at bottom
   - ✅ Stays on login screen
   - ✅ Can try again

---

## 🔍 What to Check

### **During Login:**
- [ ] Loading spinner appears
- [ ] No errors in console
- [ ] Smooth navigation to Home
- [ ] Dashboard icons visible

### **During Logout:**
- [ ] Confirmation dialog appears
- [ ] Loading indicator shows
- [ ] Returns to login screen
- [ ] Login screen is clean (no user data visible)

### **After Logout:**
- [ ] Can login again successfully
- [ ] No "already logged in" errors
- [ ] Fresh session created

---

## 🐛 Common Issues & Solutions

### **Issue: "Unauthorized" error**
- **Cause:** Token expired or invalid
- **Solution:** Logout and login again

### **Issue: App crashes on login**
- **Check:** Console for error messages
- **Solution:** Verify API credentials are correct

### **Issue: Stuck on loading**
- **Cause:** Network timeout or API down
- **Solution:** Check internet connection, verify API is accessible

---

## 📊 API Calls Being Made

### **Login:**
```
POST https://palasbari.h24x7.in/api/login
Body: username=Palashbari_Dev&password=Palashbari987@.!
Response: { access_token, refresh_token, user: {...} }
```

### **Logout:**
```
POST https://palasbari.h24x7.in/api/logout
Headers: Authorization: Bearer {access_token}
Response: Success
```

---

## ✅ Success Criteria

All tests pass if:
1. ✅ Can login with correct credentials
2. ✅ Can logout successfully
3. ✅ Can re-login after logout
4. ✅ App remembers login on restart
5. ✅ Error messages show for wrong credentials

---

## 🎯 Next Phase After Testing

Once login/logout is confirmed working:
- **Phase 2:** Connect all 24 dashboard icons to their APIs
- **Phase 3:** Build generic list screens with filters
- **Phase 4:** Add WhatsApp and Call functionality
- **Phase 5:** Profile and password management
