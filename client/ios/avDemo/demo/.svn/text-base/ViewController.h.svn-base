//
//  ViewController.h
//  demo
//
//  Created by lishengcun on 16/5/13.
//
//

#import <UIKit/UIKit.h>
#import "VCCallType.h"
#import "VCCallComing.h"
#import "VCRoomList.h"
#import "ViewController3.h"
#import "VCCallInvite.h"
#import "VCSetting.h"
#import <CoreFoundation/CoreFoundation.h>
#import <AudioToolbox/AudioToolbox.h>
#import <Foundation/Foundation.h>

#import <SystemConfiguration/SystemConfiguration.h>
#import <CoreTelephony/CTCarrier.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <CoreTelephony/CTCallCenter.h>
#import <CoreTelephony/CTCall.h>

#define MAX_STREAM_BUFFER_SIZE  2048

enum eCoreNetwork
{
    eCoreNetworkUnknown      = 0,
    eCoreNetworkNone         = 1,
    eCoreNetworkWifi         = 2,
    eCoreNetworkMobile       = 3,
    eCoreNetworkMobile2G     = 4,
    eCoreNetworkMobile3G     = 5,
    eCoreNetworkMobile4G     = 6,
};

typedef enum eRoleType{
    E_AVCALL_CALLER=0,
    E_AVCALL_CALLEE=1,
}E_ROLETYPE;

typedef enum EClientState{
    E_CLIENT_UNKNOW,
    E_CLIENT_START,
    E_CLIENT_LOGIN,
    E_CLIENT_LOGOUT,
}E_CLIENTSTATE;

typedef struct tUserInfo{
    eCoreNetwork                eNetworkState;
    SCNetworkReachabilityRef networkReachability;
    SystemSoundID myAlertSound;
    uint32_t  uiSendPingMsgRecorder;
    NSTimer *timerKeepalive;
    NSThread  *backgroundThread;
    E_CLIENTSTATE eClientState;
    NSString *uid;
    NSString *app;
    NSString *token;
}T_USERINFO, *PT_USERINFO;

typedef struct tCallSessionInfo{
    NSString *url;
    NSString *sessionID;
    NSString *peer;
    E_ROLETYPE eRoleType;
}T_CALLSESSIONINFO, *PT_CALLSESSIONINFO;

//extern NSString             *g_sessionid;

extern VCCallType  *g_vcCallType;
extern VCRoomList  *g_vcRoomList;
extern VCCallComing *g_vcCallComing;
extern ViewController3 *g_vcCalling;
extern VCCallInvite *g_vcCallInvite;
extern VCSetting    *g_vcSetting;

extern UIViewController *g_curViewController;

extern CFReadStreamRef      g_readStream;
extern CFWriteStreamRef     g_writeStream;

extern PT_USERINFO g_pUserInfo;
extern PT_CALLSESSIONINFO g_pCallSessionInfo;



@interface UserInfo : NSObject

@property NSString *uid;
@property NSString *nick;
@property NSInteger state;

@end

@interface ViewController : UIViewController{
    E_CLIENTSTATE _eClientState;
}

-(void) ReconnectMsgServer;

@property (weak, nonatomic) IBOutlet UILabel *userapptips;
@property (weak, nonatomic) IBOutlet UITextField *userapp;
@property (weak, nonatomic) IBOutlet UITextField *username;
@property (weak, nonatomic) IBOutlet UITextField *userpwd;
@property (weak, nonatomic) IBOutlet UILabel *usertips;

@end

