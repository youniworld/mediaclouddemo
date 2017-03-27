//
//  ViewController.m
//  demo
//
//  Created by lishengcun on 16/5/13.
//
//

#import <Foundation/Foundation.h>
#import <CoreFoundation/CoreFoundation.h>
#import "ViewController.h"
#import "ViewController2.h"
#import "ViewController3.h"
#import "VCCallType.h"
#import "sdk/Sdk.h"
#import "ClientMsg.h"
#import "mediaappsingnal.pb.h"
#import "VCCallComing.h"
#import "VCRoomList.h"



PT_USERINFO g_pUserInfo=NULL;
PT_CALLSESSIONINFO g_pCallSessionInfo=NULL;

//NSString             *g_sessionid;
CFReadStreamRef     g_readStream;
CFWriteStreamRef    g_writeStream;

VCCallType  *g_vcCallType=nil;
VCRoomList  *g_vcRoomList=nil;
VCCallComing *g_vcCallComing=nil;
ViewController3 *g_vcCalling=nil;
UIViewController *g_curViewController=nil;
VCCallInvite *g_vcCallInvite=nil;
VCSetting    *g_vcSetting=nil;
ViewController *g_vcViewController=nil;

eCoreNetwork CoreNetworkByFlags(SCNetworkReachabilityFlags flags)
{
    eCoreNetwork network = eCoreNetworkNone;
    if((flags & kSCNetworkReachabilityFlagsReachable)==kSCNetworkReachabilityFlagsReachable)
    {
        network = eCoreNetworkWifi;
        if((flags & kSCNetworkReachabilityFlagsIsWWAN)==kSCNetworkReachabilityFlagsIsWWAN)
        {
            network = eCoreNetworkMobile;
            CTTelephonyNetworkInfo *info = [[CTTelephonyNetworkInfo alloc] init];
            NSString *currentStatus = info.currentRadioAccessTechnology;
            if ([currentStatus isEqualToString:CTRadioAccessTechnologyGPRS])
            {
                network = eCoreNetworkMobile2G;
            }
            else if([currentStatus isEqualToString:CTRadioAccessTechnologyEdge])
            {
                network = eCoreNetworkMobile2G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyWCDMA])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyHSDPA])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyHSUPA])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyCDMA1x])
            {
                network = eCoreNetworkMobile2G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyCDMAEVDORev0])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyCDMAEVDORevA])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyCDMAEVDORevB])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyeHRPD])
            {
                network = eCoreNetworkMobile3G;
            }
            else if ([currentStatus isEqualToString:CTRadioAccessTechnologyLTE])
            {
                network = eCoreNetworkMobile4G;
            }
        }
    }
    return network;
}


void CoreNetworkCallback(SCNetworkReachabilityRef target, SCNetworkReachabilityFlags flags, void* info)
{
    
    eCoreNetwork network = CoreNetworkByFlags(flags);
    if (eCoreNetworkNone!=network) {
        if (g_pUserInfo->eNetworkState!=network) {
            //reconnect server
            if (nil!=g_vcViewController) {
                [g_vcViewController  ReconnectMsgServer];
            }
        }
    }
    g_pUserInfo->eNetworkState=network;
    
    NSLog(@"current network status is .....%d.", network);
   

}

void MonitorNetworkUsingAFNetwrok(){
     g_pUserInfo->networkReachability = SCNetworkReachabilityCreateWithName(kCFAllocatorDefault, "hifun.mobi");
    
    if(g_pUserInfo->networkReachability!=NULL)
    {
        SCNetworkReachabilityContext context = {0, NULL, NULL, NULL, NULL};
        if(!SCNetworkReachabilitySetCallback(g_pUserInfo->networkReachability, CoreNetworkCallback, &context) ||
           !SCNetworkReachabilityScheduleWithRunLoop(g_pUserInfo->networkReachability, CFRunLoopGetCurrent(), kCFRunLoopDefaultMode))
        {
            CFRelease(g_pUserInfo->networkReachability);
            g_pUserInfo->networkReachability = NULL;
        }
    }
    SCNetworkReachabilityFlags flags;
    if(SCNetworkReachabilityGetFlags(g_pUserInfo->networkReachability, &flags))
    g_pUserInfo->eNetworkState = CoreNetworkByFlags(flags);
    
/*
    SCNetworkReachabilityFlags flags;
    CoreNetwork n = CoreNetworkNone;
    if(SCNetworkReachabilityGetFlags(_networkReachability, &flags))
        n = CoreNetworkByFlags(flags);
    CoreNetworkChanged(n);
 */
}


@interface ViewController(){
    NSMutableData *_recvData;
    
}

- (void)didRecvData:(NSData *)data;
- (void)didFinishReceivingData;
@end

bool SendUserRegister(const char* szApp, const char* szName, const char* szPwd){
    bool bRtn=false;
    if(strlen(szApp)<=0 || strlen(szName)<=0 || strlen(szPwd)<=0)
        return bRtn;
    
    
    NSString *urlStr = @"http://lianmaibiz.hifun.mobi:9800/register";
    //NSString *urlStr = @"http://114.55.252.202:9800/register";
    NSURL *url = [[NSURL alloc] initWithString:urlStr];
    NSLog(@"scheme:%@ host:%@ port:%@ absoluteString:%@", [url scheme], [url host], [url port], [url absoluteString]);
    
    //NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringCacheData timeoutInterval:15];
    
    [request setValue:@"application/json; charset=utf-8" forHTTPHeaderField:@"Content-Type"];
    [request setValue:[NSString stringWithUTF8String:szApp] forHTTPHeaderField:@"portal"];
    //[request setValue:@"bj.mediacloud.app" forHTTPHeaderField:@"portal"];
    [request setHTTPMethod:@"POST"];
    
    NSMutableDictionary *dicBody = [[NSMutableDictionary alloc] init];
    [dicBody setValue:[NSString stringWithUTF8String:szName] forKey:@"uid"];
    [dicBody setValue:[NSString stringWithUTF8String:szPwd] forKey:@"pwd"];
    NSError *error=nil;
    NSData *jsonBody = [NSJSONSerialization dataWithJSONObject:dicBody options:NSJSONWritingPrettyPrinted error:&error];
    if(!([jsonBody length]>0 && error==nil)){
        NSString *strBody = [[NSString alloc] initWithData:jsonBody encoding:NSUTF8StringEncoding];
        NSLog(@"user register request body:%@", strBody);
        return bRtn;
    }
    
    [request setHTTPBody:jsonBody];
    
    NSURLResponse *rsp;
    NSData *result = [NSURLConnection sendSynchronousRequest:request returningResponse:&rsp error:&error];
    NSString *html = [[NSString alloc] initWithData:result encoding:NSUTF8StringEncoding];
    
    if (error!=nil) {
        NSString *errDesc = [error localizedDescription];
        NSLog(@"register failed. error:%@", errDesc);
        return bRtn;
    }
    
    NSHTTPURLResponse *httpRsp = (NSHTTPURLResponse*)rsp;
    NSInteger statusCode = [httpRsp statusCode];
    if(200!=statusCode){
        NSLog(@"register failed. statuscode:%d", statusCode);
        return bRtn;
    }
    
    id jsonObj = [NSJSONSerialization JSONObjectWithData:result options:NSJSONReadingMutableContainers error:&error];
    if (![jsonObj isKindOfClass:[NSDictionary class]]) {
        NSLog(@"register failed. body failed:%@", html);
        return bRtn;
    }
    
    NSDictionary *dicRsp = (NSDictionary*)jsonObj;
    NSNumber* errcode = (NSNumber*)[dicRsp valueForKey:@"errorcode"];
    if(0!=[errcode intValue] && 3000!=[errcode intValue]){
        return bRtn;
    }
    
    bRtn=true;
    return bRtn;
}

bool SendUserLogin(const char* szApp, const char* szName, const char* szPwd){
    bool bRtn=true;
    
    if(0==strcmp(szApp, "bj.mediacloud.app") && 0==strcmp(szName, "wlj") &&0==strcmp(szPwd, "wlj"))
        bRtn=true;
  
    //send login message to server
    
    
    return bRtn;
}

void readStreamCallback( CFReadStreamRef stream, CFStreamEventType event, void* myPtr ){
    ViewController* viewController = (__bridge ViewController*)myPtr;
    
    switch (event) {
        case kCFStreamEventHasBytesAvailable: {
            while (CFReadStreamHasBytesAvailable(stream)) {
                UInt8 buffer[MAX_STREAM_BUFFER_SIZE];
                int numBytesRead = CFReadStreamRead(stream, buffer, MAX_STREAM_BUFFER_SIZE);
                [viewController didRecvData:[NSData dataWithBytes:buffer length:numBytesRead]];
            }
            
            break;
        }
        case kCFStreamEventErrorOccurred: {
            CFErrorRef error = CFReadStreamCopyError(stream);
            if(CFErrorGetCode(error)!=0){
                NSString *errInfo = [NSString stringWithFormat:@"failed while reading stream; error:%@ (code %ld)", (__bridge NSString*)CFErrorGetDomain(error), CFErrorGetCode(error)];
                NSLog(errInfo);
            }
            CFRelease(error);
            break;
        }
        case kCFStreamEventEndEncountered: {
            [viewController didFinishReceivingData];
            CFReadStreamClose(stream);
            CFReadStreamUnscheduleFromRunLoop(stream, CFRunLoopGetCurrent(), kCFRunLoopCommonModes);
            CFRunLoopStop(CFRunLoopGetCurrent());
            break;
        }
        default:
            break;
    }
}




@implementation UserInfo

@end

@implementation ViewController


-(void) ReconnectMsgServer{
    g_pUserInfo->uiSendPingMsgRecorder=0;
    
    //disconnect the connect thread
    [g_pUserInfo->backgroundThread cancel];
    
    g_pUserInfo->eClientState = E_CLIENT_UNKNOW;
    NSURL *url = [NSURL URLWithString:@"http://lianmaibiz.hifun.mobi:9300"];
    g_pUserInfo->backgroundThread = [[NSThread alloc] initWithTarget:self
                                                            selector:@selector(connectClientMsgThread:) object:url];
    [g_pUserInfo->backgroundThread start];
}

-(void)ProcessLoginRspMsg:(NSString*)token Code:(NSInteger)code Reason:(NSString*)reason {
    if (0==code) {
        if (g_curViewController!=nil) {
            return;
        }
        
        //save login message to server
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        [defaults setObject:self.username.text forKey:@"username"];
        [defaults setObject:self.userpwd.text forKey:@"userpwd"];
        [defaults synchronize];
        
        NSLog(@"app:%@, name:%@, pwd:%@", self.userapp.text, self.username.text, self.userpwd.text);
      //  NSLog(@"..........................");
        
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            g_pUserInfo->token = token;
            Sdk::Instance()->Init();
            
            //[self dismissViewControllerAnimated:YES completion:nil];
            NSString *urlStr = @"http://lianmaibiz.hifun.mobi:9800/user/all";
            NSURL *url = [[NSURL alloc] initWithString:urlStr];
            
            NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringCacheData timeoutInterval:5];
            [request setValue:g_pUserInfo->token forHTTPHeaderField:@"token"];
            [request setValue:g_pUserInfo->app forHTTPHeaderField:@"portal"];
            [request setHTTPMethod:@"GET"];
            
            NSURLResponse *rsp=nil;
            NSError *error=nil;
            NSData *result = [NSURLConnection sendSynchronousRequest:request returningResponse:&rsp error:&error];
            NSString *html = [[NSString alloc] initWithData:result encoding:NSUTF8StringEncoding];
            if (error!=nil) {
                NSString *errDesc = [error localizedDescription];
                NSLog(@"http get user list failed. err:%@", errDesc);
                return;
            }
            
            NSHTTPURLResponse *httpRsp = (NSHTTPURLResponse*)rsp;
            NSInteger statusCode = [httpRsp statusCode];
            if(200!=statusCode){
                NSLog(@"http get user list failed statuscode:%d", statusCode);
                return;
            }
            id jsonObj = [NSJSONSerialization JSONObjectWithData:result options:NSJSONReadingMutableContainers error:&error];
            
            if (![jsonObj isKindOfClass:[NSDictionary class]]) {
                NSLog(@"http get user list failed body:%@", html);
                return;
            }
            
            NSDictionary *dicRsp = (NSDictionary*)jsonObj;
            NSArray *userList = [dicRsp objectForKey:@"users"];
            
            NSMutableArray *userArray = [[NSMutableArray alloc] init];
            
            for (NSDictionary *user in userList ) {
                UserInfo *userinfo = [[UserInfo alloc] init];
                [userinfo setUid:[user objectForKey:@"uid"]];
                [userinfo setNick:[user objectForKey:@"nick"]];
                [userinfo setState:[[user objectForKey:@"state"] intValue]];
                [userArray addObject:userinfo];
                NSLog(@"add user uid:%@ nick:%@ state:%d", [userinfo uid], [userinfo nick], [userinfo state]);
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                g_vcRoomList.viewData = userArray;
                [self presentViewController:g_vcRoomList animated:YES completion:nil];
                g_curViewController = g_vcRoomList;
            });
        });
    }
    else{
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.usertips.text initWithFormat:@"login failed. error:%@", reason];
        });
    }
    
    //start a timer to send ping message to server
    
    g_pUserInfo->uiSendPingMsgRecorder=0;
    g_pUserInfo->timerKeepalive  = [NSTimer timerWithTimeInterval:120 target:self selector:@selector(SendPingMessage) userInfo:nil repeats:YES];
    [[NSRunLoop mainRunLoop] addTimer:g_pUserInfo->timerKeepalive forMode:NSRunLoopCommonModes];
    
}

-(void) SendPingMessage{
    
    //send ping message to server
    if (g_pUserInfo==NULL && g_pUserInfo->eClientState!=E_CLIENT_START) {
        return;
    }
    
    NSLog(@"timer is running. index:%d", g_pUserInfo->uiSendPingMsgRecorder);
    if (g_pUserInfo->uiSendPingMsgRecorder>=3) {
        
        NSLog(@"timer is running reconnect server...... index:%d", g_pUserInfo->uiSendPingMsgRecorder);
        [self ReconnectMsgServer];
        return;
    }
  
    MediaAppSignalMessage msg;
    MediaSignalMessage* pSigMsg=msg.mutable__signal();
    MediaSignalMessage_ping *pPingMsg = pSigMsg->mutable__ping();
    
    uint32_t iMsgLen = msg.ByteSize();
    
    UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
    msg.SerializeToArray(sendBuf+6, iMsgLen);
    pSigMsg->set_allocated__login(NULL);
    msg.set_allocated__signal(NULL);
    
    sendBuf[0] = 0xFA;
    sendBuf[1] = 0xAF;
    uint32_t iMsgLenBigEnd = htonl(iMsgLen);
    memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
    
    // msg.SerializeToArray();
    CFIndex sendLen=6+iMsgLen;
    CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
    
    g_pUserInfo->uiSendPingMsgRecorder++;
    
}

-(void)ProcessAnMessage:(NSData*)data{
    NSLog(@"Process An Message. msglen:%d", [data length]);
    MediaAppSignalMessage msg;
    msg.ParseFromArray([data bytes], [data length]);
    
    if (msg.has__signal()) {
        //recv login response message
        MediaSignalMessage sigMsg = msg._signal();
        if (sigMsg.has__loginresp()) {
            MediaSignalMessage_LoginResp loginRspMsg = sigMsg._loginresp();
            [self ProcessLoginRspMsg:[NSString stringWithFormat:@"%s", loginRspMsg._token().c_str()] Code:loginRspMsg._code() Reason:[NSString stringWithFormat:@"%s", loginRspMsg._failed_reason().c_str()]];
        } else if(sigMsg.has__statechanged()) {
            MediaSignalMessage_OnlineStateChange stateMsg = sigMsg._statechanged();
            
            std::string strUid = stateMsg._uid();
            uint32_t iState = stateMsg._state();
            
            if (g_vcRoomList!=nil) {
                [g_vcRoomList changeUserState:strUid.c_str() State:iState];
            }
        } else if(sigMsg.has__pong()){
            g_pUserInfo->uiSendPingMsgRecorder=0;
            NSLog(@"recv a pong message from server. index:%d", g_pUserInfo->uiSendPingMsgRecorder);
        }
        
    } else if(msg.has__call()) {
        MediaCallMessage callMsg = msg._call();
        
        if (callMsg.has__call_initiate()) {
            MediaCallMessage_BaseMesssage BaseMsg = callMsg._base();
            MediaCallMessage_CallInitiate initMsg = callMsg._call_initiate();
            
            if (NULL!=g_pCallSessionInfo) {
                MediaAppSignalMessage appMsg;
                MediaCallMessage *pCallMsg = appMsg.mutable__call();
                MediaCallMessage_CallTerminate *pTermMsg = pCallMsg->mutable__call_terminate();
                MediaCallMessage_BaseMesssage *pBaseMsg = pCallMsg->mutable__base();
                pBaseMsg->set__from([g_pUserInfo->uid UTF8String]);
                pBaseMsg->set__to([g_pCallSessionInfo->peer UTF8String]);
                pBaseMsg->set__portal([g_pUserInfo->app UTF8String]);
                
                pTermMsg->set__callid(initMsg._callid().c_str());
                pTermMsg->set__reason(1);
                
                uint32_t iMsgLen = appMsg.ByteSize();
                UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
                appMsg.SerializeToArray(sendBuf+6, iMsgLen);
                pCallMsg->set_allocated__call_terminate(NULL);
                pCallMsg->set_allocated__base(NULL);
                appMsg.set_allocated__call(NULL);
                sendBuf[0] = 0xFA;
                sendBuf[1] = 0xAF;
                uint32_t iMsgLenBigEnd = htonl(iMsgLen);
                memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
                CFIndex sendLen = 6+iMsgLen;
                CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
                
                NSLog(@"the current is state is calling.");
                return;
            }
            
            g_pCallSessionInfo = new T_CALLSESSIONINFO;
            g_pCallSessionInfo->sessionID = [NSString stringWithFormat:@"%s", initMsg._callid().c_str()];
            g_pCallSessionInfo->peer = [NSString  stringWithUTF8String:initMsg._caller().c_str()];
            //g_pCallSessionInfo->peer = [NSString stringWithFormat:@"%s", initMsg._caller().c_str()];
            g_pCallSessionInfo->eRoleType = E_AVCALL_CALLEE;
            
            NSLog(@"recv a call from %@ sessionid:%@ curvc:%p", g_pCallSessionInfo->peer, g_pCallSessionInfo->sessionID, g_curViewController);
            
            dispatch_async(dispatch_get_main_queue(), ^{
                
                NSURL  *url = [NSURL URLWithString:@"/Library/Ringtones/Opening.m4r"];
                AudioServicesCreateSystemSoundID((__bridge CFURLRef)(url), &(g_pUserInfo->myAlertSound));
                AudioServicesPlaySystemSound(g_pUserInfo->myAlertSound);
                
                [g_curViewController presentViewController:g_vcCallComing animated:YES completion:nil];
                [g_vcCallComing setCallerInfo:[g_pCallSessionInfo->peer UTF8String]];
                g_curViewController = g_vcCallComing;
                
                //[g_vcRoomList ProcessCallComing];
            });
            
          /*
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    
                    //VCCallComing *vcCallComing = [[VCCallComing alloc] init];
                    g_vcCallComing =[[VCCallComing alloc] init];
                    [g_vcRoomList presentViewController:g_vcCallComing animated:YES completion:nil];
                    [g_vcCallComing setCallerInfo:initMsg._caller().c_str()];
                    g_curViewController = g_vcCallComing;
                    //[self performSelectorOnMainThread:@selector(ProcessCallComingMsg) withObject:nil waitUntilDone:YES];
                });
            });
            */
        } else if(callMsg.has__call_terminate()){
            if (g_curViewController==g_vcRoomList || g_curViewController==g_vcSetting) {
                return;
            }
            
            MediaCallMessage_BaseMesssage BaseMsg = callMsg._base();
            MediaCallMessage_CallTerminate termMsg = callMsg._call_terminate();
            uint32_t uiReason = termMsg._reason();
            
            if(g_curViewController == g_vcCallInvite){
                 dispatch_async(dispatch_get_main_queue(), ^{
                     [g_vcCallInvite setTips:@"peer is busy..."];
                });
                return;
            }
            
            if(0 == uiReason){
                if (g_curViewController==g_vcCallComing) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        if (g_curViewController==g_vcCallComing) {
                            [g_vcCallComing  setCallerInfo:"peer terminate the call"];
                        }
                        
                    });
                    return;
                }
                else if(g_curViewController==g_vcCalling){
                    if (g_curViewController==g_vcCalling) {
                        [g_vcCalling StopCall];
                    }
                }
                
                //stop the call and disvisible the viewcontroller
            } else if(1==uiReason){

                //the peer reject the call and the caller terminate the session before the callee accept the session
                dispatch_async(dispatch_get_main_queue(), ^{
                    [g_curViewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
                });
            }
        }else if(callMsg.has__call_accept()){
            dispatch_async(dispatch_get_main_queue(), ^{
                g_vcCalling = [[ViewController3 alloc] init];
                [g_vcCalling StartCall];
                [g_curViewController presentViewController:g_vcCalling animated:YES completion:nil];
                g_curViewController = g_vcCalling;
                
            });
            
            
            
        }
    }
    
}

-(void) touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    
    UITouch *touch = [touches anyObject];
  //  CGPoint touchPoint = [touch locationInView:self];
    if (touch.tapCount==2) {
        self.userapptips.hidden = !self.userapptips.isHidden;
        self.userapp.hidden = !self.userapp.isHidden;
    }else if(touch.tapCount==1){
       // NSLog(@"touch one");
        [self.view endEditing:YES];
        //[self.userpwd resignFirstResponder];
    }
    
}

-(int)cutAnMessage{
    int iRtn=0;
    
    uint8_t FlagFA =(((uint8_t*)[_recvData bytes])[0]);
    uint8_t FlagAF =(((uint8_t*)[_recvData bytes])[1]);
    
    if (0xFA!=FlagFA || 0xAF!=FlagAF) {
        return 2;
    }
    uint32_t PackLenBigEnd = 0;
    memcpy(&PackLenBigEnd,  ((uint8_t*)[_recvData bytes])+2, 4);
    uint32_t PackLen = ntohl(PackLenBigEnd);
    
    if(PackLen < [_recvData length]-6)
        return iRtn;
    iRtn = PackLen+6;
    
    NSData *subData = [_recvData subdataWithRange:NSMakeRange(6, iRtn-6)];
    [self ProcessAnMessage:subData];
    
    return iRtn;
}

-(void)didRecvData:(NSData*)data{
    if(_recvData == nil){
        _recvData = [[NSMutableData alloc] init];
    }
    
    [_recvData appendData:data];
    NSLog(@"recv message msgsize:%d", [data length] );
    
    int iRtn = [self cutAnMessage];
    if (0<iRtn) {
        [_recvData replaceBytesInRange:NSMakeRange(0, iRtn) withBytes:NULL length:0];
    }

    //process message from server
    
    
}

- (void)didFinishReceivingData{
    CFWriteStreamClose(g_writeStream);
    g_writeStream=nil;
    g_readStream=nil;
    NSLog(@"finish................................. ");
    
}

-(void)TestNetworkUsingStatusBar{
    UIApplication *app = [UIApplication sharedApplication];
    NSArray *children = [[[app valueForKeyPath:@"statusBar"] valueForKeyPath:@"foregroundView" ] subviews];
    NSString *state = [[NSString alloc]init];
    int netType=0;
    
    for (id child in children) {
        if ([child isKindOfClass:NSClassFromString(@"UIStatusBarDataNetworkItemView")]) {
            netType = [[child valueForKeyPath:@"dataNetworkType"] intValue];
            switch (netType) {
                case 0:
                    state = @"无网络";
                    break;
                case 1:
                    state = @"2G";
                    break;
                case 2:
                    state = @"3G";
                    break;
                case 3:
                    state = @"4G";
                    break;
                case 5:
                    state = @"wifi";
                    break;
                    
                default:
                    break;
            }
        }
    }
    NSLog(@"the current netwrok status is %@......", state);
    return;
}



- (IBAction)OnLogin:(UIButton *)sender {
    
    [self.view endEditing:YES];
    
    g_vcViewController = self;
    
    //create background thread connect to client message server
    if (NULL!=g_pUserInfo) {
        [g_pUserInfo->backgroundThread cancel];
        delete g_pUserInfo;
        g_pUserInfo=NULL;
    }
    
    g_pUserInfo = new T_USERINFO;
    g_pUserInfo->eClientState = E_CLIENT_UNKNOW;
    
    NSURL *url = [NSURL URLWithString:@"http://lianmaibiz.hifun.mobi:9300"];
    g_pUserInfo->backgroundThread = [[NSThread alloc] initWithTarget:self
                                                            selector:@selector(connectClientMsgThread:) object:url];
    [g_pUserInfo->backgroundThread start];

    
    NSLog(@"OnLogin app:%@ name:%@ pwd:%@", self.userapp.text, self.username.text, self.userpwd.text);
   

    g_pUserInfo->app = self.userapp.text;
    g_pUserInfo->uid = self.username.text;
    
    MonitorNetworkUsingAFNetwrok();
    
    /*
    MediaAppSignalMessage msg;
    MediaSignalMessage* pSigMsg=msg.mutable__signal();
    MediaSignalMessage_Login *pLogMsg =pSigMsg->mutable__login();
    pLogMsg->set__uid([self.username.text UTF8String]);
    pLogMsg->set__pwd([self.userpwd.text UTF8String]);
    pLogMsg->set__portal([self.userapp.text UTF8String]);
    uint32_t iMsgLen = msg.ByteSize();
  
    
    UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
    msg.SerializeToArray(sendBuf+6, iMsgLen);
    pSigMsg->set_allocated__login(NULL);
    msg.set_allocated__signal(NULL);
    
    sendBuf[0] = 0xFA;
    sendBuf[1] = 0xAF;
    uint32_t iMsgLenBigEnd = htonl(iMsgLen);
    memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
    
   // msg.SerializeToArray();
    CFIndex sendLen=6+iMsgLen;
    CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
    NSLog(@"send login message to server msglen:%d", factSendLen);
    */
    
    
    


}
- (IBAction)OnRegister:(UIButton *)sender {
    [self.view endEditing:YES];
    NSLog(@"OnRegister app:%@ name:%@ pwd:%@", self.userapp.text, self.username.text, self.userpwd.text);
    bool bRtn = SendUserRegister( [self.userapp.text UTF8String], [self.username.text UTF8String], [self.userpwd.text UTF8String] );
    
    if (bRtn) {
        self.usertips.text = @"register successed.";
    }
    else{
        self.usertips.text = @"register failed.";
    }

}


- (void)viewDidLoad
{
    [super viewDidLoad];
    
   // self.userapp.text = @"bj.mediacloud.app";
     self.userapp.text = @"yaobo.mediacloud.app";
    //self.username.text = @"123";
    //self.userpwd.text= @"123";
    self.userapptips.hidden = YES;
    self.userapp.hidden = YES;
    
    g_vcCallType    = [[VCCallType alloc] init];
    g_vcRoomList    = [[VCRoomList alloc] init];
    g_vcSetting     = [[VCSetting alloc] init];
    g_vcCallInvite  = [[VCCallInvite alloc] init];
    g_vcCallComing  = [[VCCallComing alloc] init];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    self.username.text = [defaults objectForKey:@"username"];
    self.userpwd.text = [defaults objectForKey:@"userpwd"];
    
    if (self.username.text.length>0 && self.userpwd.text.length>0) {
        g_vcViewController = self;
        
        //create background thread connect to client message server
        if (NULL!=g_pUserInfo) {
            [g_pUserInfo->backgroundThread cancel];
            delete g_pUserInfo;
            g_pUserInfo=NULL;
        }
        
        g_pUserInfo = new T_USERINFO;
        g_pUserInfo->eClientState = E_CLIENT_UNKNOW;
        
        NSURL *url = [NSURL URLWithString:@"http://lianmaibiz.hifun.mobi:9300"];
        g_pUserInfo->backgroundThread = [[NSThread alloc] initWithTarget:self
                                                                selector:@selector(connectClientMsgThread:) object:url];
        [g_pUserInfo->backgroundThread start];
        
        
        NSLog(@"OnLogin app:%@ name:%@ pwd:%@", self.userapp.text, self.username.text, self.userpwd.text);
        
        
        g_pUserInfo->app = self.userapp.text;
        g_pUserInfo->uid = self.username.text;
        
        MonitorNetworkUsingAFNetwrok();

    }
}

-(void)connectClientMsgThread:(NSURL*)url{
    NSLog(@"background thread is running.........");
    
    NSString *host = [url host];
    NSInteger port = [[url port] integerValue];
    
    CFStreamClientContext ctx = {0, (__bridge void *)(self), NULL, NULL, NULL};
    CFOptionFlags registeredEvents = (kCFStreamEventHasBytesAvailable | kCFStreamEventEndEncountered | kCFStreamEventErrorOccurred);
    
    CFStreamCreatePairWithSocketToHost(kCFAllocatorDefault, (__bridge CFStringRef)host, port, &g_readStream, &g_writeStream);
    
    if(CFReadStreamSetClient(g_readStream, registeredEvents, readStreamCallback, &ctx)){
        CFReadStreamScheduleWithRunLoop(g_readStream, CFRunLoopGetCurrent(), kCFRunLoopCommonModes);
    }
    else{
        NSLog(@"failed to assign callback background thread.");
        return;
    }
    
    if(CFReadStreamOpen(g_readStream)==NO || CFWriteStreamOpen(g_writeStream)==NO){
        NSLog(@"failed to open read stream or failed to open write stream.");
        return;
    }
    
    CFErrorRef error = CFReadStreamCopyError(g_readStream);
    if(NULL!=error){
        if(CFErrorGetCode(error)!=0){
            NSString *errorInfo = [NSString stringWithFormat:@"failed to connect stream; error '%@' (code %ld)", (__bridge NSString*)CFErrorGetDomain(error), CFErrorGetCode(error)];
            NSLog(errorInfo);
        }
        CFRelease(error);
        return;
    }
    
    NSLog(@"successfully connected to %@", url);
    g_pUserInfo->eClientState = E_CLIENT_START;
    
    //send login message to server
    
    MediaAppSignalMessage msg;
    MediaSignalMessage* pSigMsg=msg.mutable__signal();
    MediaSignalMessage_Login *pLogMsg =pSigMsg->mutable__login();
    pLogMsg->set__uid([self.username.text UTF8String]);
    pLogMsg->set__pwd([self.userpwd.text UTF8String]);
    pLogMsg->set__portal([self.userapp.text UTF8String]);
    uint32_t iMsgLen = msg.ByteSize();
    
    
    UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
    msg.SerializeToArray(sendBuf+6, iMsgLen);
    pSigMsg->set_allocated__login(NULL);
    msg.set_allocated__signal(NULL);
    
    sendBuf[0] = 0xFA;
    sendBuf[1] = 0xAF;
    uint32_t iMsgLenBigEnd = htonl(iMsgLen);
    memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
    
    // msg.SerializeToArray();
    CFIndex sendLen=6+iMsgLen;
    CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
    NSLog(@"send login message to server msglen:%d", factSendLen);

    
    CFRunLoopRun();
}



@end
