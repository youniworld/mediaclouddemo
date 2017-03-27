//
//  VCSetting.m
//  demo
//
//  Created by wlj on 2016/11/21.
//
//

#import <Foundation/Foundation.h>
#import "VCSetting.h"
#import "mediaappsingnal.pb.h"
#import "ViewController.h"


@implementation VCSetting

-(void) viewDidLoad{
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor whiteColor];

    UILabel* back = [[UILabel alloc] init];
    back.backgroundColor = [UIColor orangeColor];
    back.text = @"Back";
    back.frame = CGRectMake(self.view.bounds.size.width-120, self.view.bounds.size.height-60, 120, 60);
    back.textAlignment = NSTextAlignmentCenter;
    back.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    back.userInteractionEnabled = YES;
    [back addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickBackBtn:)]];
    [self.view addSubview:back];
    
    UILabel* Logout = [[UILabel alloc] init];
    Logout.backgroundColor = [UIColor orangeColor];
    Logout.text = @"Logout";
    Logout.frame = CGRectMake(0, self.view.bounds.size.height-60, 120, 60);
    Logout.textAlignment = NSTextAlignmentCenter;
    Logout.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    Logout.userInteractionEnabled = YES;
    [Logout addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickLogoutBtn:)]];
    [self.view addSubview:Logout];
    
}

-(void)onClickBackBtn:(id)gesture{
    
    g_curViewController = self.presentingViewController;
    [self dismissViewControllerAnimated:YES completion:nil];
    
    NSLog(@"presenting:%p cur:%p presented:%p", self.presentingViewController, self,  self.presentedViewController);
}

-(void)onClickLogoutBtn:(id)gesture{
     
    [g_pUserInfo->timerKeepalive invalidate];
    
    //send logout message to server
    MediaAppSignalMessage msg;
    MediaSignalMessage* pSigMsg=msg.mutable__signal();
    MediaSignalMessage_Logout *pLogoutMsg=pSigMsg->mutable__logout();
    pLogoutMsg->set__uid([g_pUserInfo->uid UTF8String]);
    
    uint32_t iMsgLen = msg.ByteSize();
    UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
    msg.SerializeToArray(sendBuf+6, iMsgLen);
    pSigMsg->set_allocated__logout(NULL);
    msg.set_allocated__signal(NULL);
    
    sendBuf[0] = 0xFA;
    sendBuf[1] = 0xAF;
    uint32_t iMsgLenBigEnd = htonl(iMsgLen);
    memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
    
    // msg.SerializeToArray();
    CFIndex sendLen=6+iMsgLen;
    CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
    NSLog(@"send logout message to server msglen:%d", factSendLen);

    if(NULL!=g_pUserInfo){
        [g_pUserInfo->backgroundThread cancel];
        delete g_pUserInfo;
        g_pUserInfo = NULL;
    }
    
    g_curViewController = self.presentingViewController.presentingViewController;
    [self.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    g_curViewController=nil;
}

@end
