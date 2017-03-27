//
//  VCCallInvite.m
//  demo
//
//  Created by wlj on 2016/11/19.
//
//

#import <Foundation/Foundation.h>
#import "VCCallInvite.h"
#import "mediaappsingnal.pb.h"
#import "ViewController.h"

@interface VCCallInvite() {
    UILabel* _tips;
}

@end

@implementation VCCallInvite

-(void)setTips:(NSString*)infos{
    [_tips setText:infos];
}

-(void) viewDidLoad{
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    _tips = [[UILabel alloc] init];
    _tips.backgroundColor = [UIColor whiteColor];
    _tips.text = @"tips info...";
    _tips.frame = CGRectMake(0, self.view.bounds.size.height/2, self.view.bounds.size.width, 60);
    _tips.textAlignment = NSTextAlignmentCenter;
    _tips.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    [self.view addSubview:_tips];
    
    UILabel* HangUp = [[UILabel alloc] init];
    HangUp.backgroundColor = [UIColor orangeColor];
    HangUp.text = @"Hangup";
    HangUp.frame = CGRectMake(self.view.bounds.size.width/2-60, self.view.bounds.size.height-60, 120, 60);
    HangUp.textAlignment = NSTextAlignmentCenter;
    HangUp.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    HangUp.userInteractionEnabled = YES;
    [HangUp addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickHangUpBtn:)]];
    [self.view addSubview:HangUp];
    
}

-(void)onClickHangUpBtn:(id)gesture{
    
     dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
         //send terminate message to caller
         
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

         dispatch_async(dispatch_get_main_queue(), ^{
             g_curViewController = self.presentingViewController;
             [self.presentingViewController dismissViewControllerAnimated:YES completion:nil];
             
             if (NULL!=g_pCallSessionInfo) {
                 delete g_pCallSessionInfo;
                 g_pCallSessionInfo = NULL;
             }
         });
     });
     
    
    
}

@end


