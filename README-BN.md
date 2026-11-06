# ARCER AI — প্রথম Buildable Android সংস্করণ

## কী আছে
- Android Studio / GitHub Actions দিয়ে APK build করার উপযোগী Android project
- Premium Black + Blue UI
- বাটন চেপে বাংলা voice recognition (ফোনের Speech Recognition service থাকলে)
- বাংলা Text-to-Speech উত্তর

## কী এখনো নেই
- “হে ARCER” wake-word detection
- স্ক্রিন বন্ধ অবস্থায় always-on microphone service
- পূর্ণ online AI API সংযোগ
- পূর্ণ offline AI model

এই ফিচারগুলো আলাদা করে বাস্তবায়ন ও পরীক্ষা করতে হবে। এই ZIP-কে সম্পূর্ণ AI বা always-on assistant হিসেবে ধরে নেবেন না।

## GitHub-এ আপলোড
1. ZIP extract করুন।
2. Extract করা `ARCER-AI` ফোল্ডারের **ভেতরের সব ফাইল ও ফোল্ডার** GitHub repository-এর root-এ upload করুন.
3. নিশ্চিত করুন `.github/workflows/android.yml` ফাইলটি আছে।
4. GitHub → Actions → Build ARCER APK → Run workflow চাপুন।
5. সবুজ হলে workflow run খুলে Artifacts থেকে `ARCER-APK` download করুন।
