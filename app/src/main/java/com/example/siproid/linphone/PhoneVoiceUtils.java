package com.example.siproid.linphone;

import org.linphone.core.Core;

public class PhoneVoiceUtils {
  private static final String TAG="PhoneVoiceUtils";
  private static volatile PhoneVoiceUtils sPhoneVoiceUrils;
  private Core core=null;

  public static PhoneVoiceUtils getInstance(){
      if (sPhoneVoiceUrils==null){
          synchronized (PhoneVoiceUtils.class){
              if (sPhoneVoiceUrils==null){
                  sPhoneVoiceUrils=new PhoneVoiceUtils();
              }
          }
      }
      return sPhoneVoiceUrils;
  }

  private PhoneVoiceUtils(){

  }
}
