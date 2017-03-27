//
//  VCCallComing.m
//  demo
//
//  Created by wlj on 2016/11/14.
//
//

#import <Foundation/Foundation.h>

#import "VCCallComing.h"
#import "mediaappsingnal.pb.h"
#import "ViewController.h"
#import "ViewController3.h"
#import <AudioToolbox/AudioToolbox.h>

@interface VCCallComing(){
    SystemSoundID _myAlertSound;
    UILabel* _comingTips;
}

@end

@implementation VCCallComing


-(void)setCallerInfo:(const char*)caller{
   // [_comingTips setText:[NSString stringWithUTF8String:"coming call from %s", caller]];
     [_comingTips setText:[NSString stringWithFormat:@"coming call from %@" ,[NSString stringWithUTF8String:caller]]];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    _comingTips = [[UILabel alloc] init];
    _comingTips.backgroundColor = [UIColor whiteColor];
    _comingTips.text = @"coming call from ...";
    //_comingTips.text = g_pCallSessionInfo->peer;
    _comingTips.frame = CGRectMake(0, self.view.bounds.size.height/2, self.view.bounds.size.width, 60);
    _comingTips.textAlignment = NSTextAlignmentCenter;
    _comingTips.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    [self.view addSubview:_comingTips];
    
    
    UILabel* acceptBtn = [[UILabel alloc] init];
    acceptBtn.backgroundColor = [UIColor orangeColor];
    acceptBtn.text = @"accept";
    acceptBtn.frame = CGRectMake(0, self.view.bounds.size.height-60, 120, 60);
    acceptBtn.textAlignment = NSTextAlignmentCenter;
    acceptBtn.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    acceptBtn.userInteractionEnabled = YES;
    [acceptBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickAcceptBtn:)]];
    [self.view addSubview:acceptBtn];
    
    UILabel* rejectBtn = [[UILabel alloc] init];
    rejectBtn.backgroundColor = [UIColor orangeColor];
    rejectBtn.text = @"reject";
    rejectBtn.frame = CGRectMake(self.view.bounds.size.width-120, self.view.bounds.size.height-60, 120, 60);
    rejectBtn.textAlignment = NSTextAlignmentCenter;
    rejectBtn.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    rejectBtn.userInteractionEnabled = YES;
    [rejectBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickRejectBtn:)]];
    [self.view addSubview:rejectBtn];
    
    /*
    NSURL  *url = [NSURL URLWithString:@"/Library/Ringtones/Opening.m4r"];
    AudioServicesCreateSystemSoundID((__bridge CFURLRef)(url), &_myAlertSound);
    AudioServicesPlaySystemSound(_myAlertSound);
    */
    // Do any additional setup after loading the view, typically from a nib.
}

-(void)onClickAcceptBtn:(id)gesture{
    AudioServicesDisposeSystemSoundID(g_pUserInfo->myAlertSound);
    
    //accept the peer's call
    //send accept message
    MediaAppSignalMessage appMsg;
    MediaCallMessage *pCallMsg = appMsg.mutable__call();
    MediaCallMessage_CallAccept *pAcceptMsg = pCallMsg->mutable__call_accept();
    MediaCallMessage_BaseMesssage *pBaseMsg = pCallMsg->mutable__base();
    pBaseMsg->set__from([g_pUserInfo->uid UTF8String]);
    pBaseMsg->set__to([g_pCallSessionInfo->peer UTF8String]);
    pBaseMsg->set__portal([g_pUserInfo->app UTF8String]);
    
    pAcceptMsg->set__callid([g_pCallSessionInfo->sessionID UTF8String]);
    pAcceptMsg->set__callee([g_pUserInfo->uid UTF8String]);
    
    uint32_t iMsgLen = appMsg.ByteSize();
    UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
    appMsg.SerializeToArray(sendBuf+6, iMsgLen);
    pCallMsg->set_allocated__call_accept(NULL);
    pCallMsg->set_allocated__base(NULL);
    sendBuf[0]=0xFA;
    sendBuf[1]=0xAF;
    uint32_t iMsgLenBigEnd = htonl(iMsgLen);
    memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
    CFIndex sendLen = 6+iMsgLen;
    CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
    if(factSendLen!=sendLen)
        return;
    
    //view calling interface
    g_vcCalling = [[ViewController3 alloc] init];
    [g_vcCalling StartCall];
    [self presentViewController:g_vcCalling animated:YES completion:nil];
    g_curViewController = g_vcCalling;
}

-(void)onClickRejectBtn:(id)gesture{
    AudioServicesDisposeSystemSoundID(g_pUserInfo->myAlertSound);
    
    MediaAppSignalMessage appMsg;
    MediaCallMessage *pCallMsg = appMsg.mutable__call();
    MediaCallMessage_CallTerminate *pTermMsg = pCallMsg->mutable__call_terminate();
    MediaCallMessage_BaseMesssage *pBaseMsg = pCallMsg->mutable__base();
    pBaseMsg->set__from([g_pUserInfo->uid UTF8String]);
    pBaseMsg->set__to([g_pCallSessionInfo->peer UTF8String]);
    pBaseMsg->set__portal([g_pUserInfo->app UTF8String]);
    
    pTermMsg->set__callid([g_pCallSessionInfo->sessionID UTF8String]);
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
    
    NSLog(@"reject the call coming. sid:%@ caller:%@", g_pCallSessionInfo->sessionID, g_pCallSessionInfo->peer);
    delete g_pCallSessionInfo;
    g_pCallSessionInfo = NULL;
    
    g_curViewController = self.presentingViewController;
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
