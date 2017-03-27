//
//  ViewController3.m
//  demo
//
//  Created by lishengcun on 16/8/15.
//
//

#import "ViewController3.h"
#import <Foundation/Foundation.h>
#import <ReplayKit/ReplayKit.h>
#import <AVFoundation/AVFoundation.h>
#import <AVFoundation/AVCaptureOutput.h>
#import <AVFoundation/AVCaptureInput.h>
#import <AVFoundation/AVCaptureDevice.h>
#import <AVFoundation/AVMediaFormat.h>
#import "sdk/Sdk.h"
#import "ios/IosVideoView.h"
#import "ViewController.h"
#import "mediaappsingnal.pb.h"

@interface ViewController3 ()
{
    AVCaptureSession* _session;
    UIImageView* _imageView;
    const char* _url;
    std::string _strUrl;
    UILabel* _recordBtn;
    UILabel* _facingBtn;
    UILabel* _beautifyBtn;
    VideoView* _previewView;
}

@property (strong,nonatomic)AVAudioPlayer * player;
@end

@implementation ViewController3

-(void)createMusic{
    NSURL * url = [NSURL fileURLWithPath:[[NSBundle mainBundle]pathForResource:@"a" ofType:@"mp3"]];
    AVAudioPlayer * player = [[AVAudioPlayer alloc]initWithContentsOfURL:url error:nil];
    player.volume = 1.0;
    self.player = player;
    [player prepareToPlay];
    [player play];
    [[RPScreenRecorder sharedRecorder]startRecordingWithMicrophoneEnabled:YES handler:^(NSError * _Nullable error) {
        
    }];
}
- (void)viewDidLoad
{
    [super viewDidLoad];
//    [self createMusic];
    
   // _strUrl = "";
    
    VideoView* videoView = [[VideoView alloc] init];
    videoView.frame = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
    videoView.streamId = 100;
    videoView.mode = ViewScaleModeClipToBounds;
    [self.view addSubview:videoView];
    
    VideoView* previewView = [[VideoView alloc] init];
    
    int previewWidth = self.view.bounds.size.width / 2 - 20;
    int previewHeight = self.view.bounds.size.height / 2 - 20;
    previewView.frame = CGRectMake(self.view.bounds.size.width - previewWidth - 10, self.view.bounds.size.height - 50 - previewHeight - 10, previewWidth, previewHeight);
    previewView.streamId = 101;
    previewView.backgroundColor = [UIColor whiteColor];
    previewView.mode = ViewScaleModeClipToBounds;
    [self.view addSubview:previewView];
    previewView.hidden = true;
    previewView = previewView;
  
    /*
    UILabel* startBtn = [[UILabel alloc] init];
    startBtn.backgroundColor = [UIColor brownColor];
    startBtn.text = @"start";
    startBtn.frame = CGRectMake(0, 20, 100, 60);
    startBtn.textAlignment = NSTextAlignmentCenter;
    startBtn.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    startBtn.userInteractionEnabled = YES;
    [startBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onStart:)]];
    [self.view addSubview:startBtn];
    */
    
    UILabel* stopBtn = [[UILabel alloc] init];
    stopBtn.backgroundColor = [UIColor orangeColor];
    stopBtn.text = @"stop";
    stopBtn.frame = CGRectMake(self.view.bounds.size.width-100, 20, 100, 60);
    stopBtn.textAlignment = NSTextAlignmentCenter;
    stopBtn.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    stopBtn.userInteractionEnabled = YES;
    [stopBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onStop:)]];
    [self.view addSubview:stopBtn];
    
    _recordBtn = [[UILabel alloc] init];
    _recordBtn.backgroundColor = [UIColor orangeColor];
    _recordBtn.text = (Sdk::Instance()->GetPushRecord() ? @"recording" : @"record");
    _recordBtn.frame = CGRectMake((self.view.bounds.size.width-100)/2, self.view.bounds.size.height-60, 100, 60);
    _recordBtn.textAlignment = NSTextAlignmentCenter;
    _recordBtn.userInteractionEnabled = YES;
    [_recordBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onRecord:)]];
    [self.view addSubview:_recordBtn];
    
    _facingBtn = [[UILabel alloc] init];
    _facingBtn.backgroundColor = [UIColor orangeColor];
    _facingBtn.text = (Sdk::Instance()->GetCameraFront() ? @"front" : @"back");
    _facingBtn.frame = CGRectMake(0, self.view.bounds.size.height-60, 100, 60);
    _facingBtn.textAlignment = NSTextAlignmentCenter;
    _facingBtn.userInteractionEnabled = YES;
    [_facingBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onFacing:)]];
    [self.view addSubview:_facingBtn];
    
    _beautifyBtn = [[UILabel alloc] init];
    _beautifyBtn.backgroundColor = [UIColor orangeColor];
    _beautifyBtn.text = (Sdk::Instance()->GetCameraBeautify() ? @"beautify" : @"normal");
    _beautifyBtn.frame = CGRectMake(self.view.bounds.size.width-100, self.view.bounds.size.height-60, 100, 60);
    _beautifyBtn.textAlignment = NSTextAlignmentCenter;
    _beautifyBtn.userInteractionEnabled = YES;
    [_beautifyBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onBeautify:)]];
    [self.view addSubview:_beautifyBtn];
}

-(void)StartCall{
    _url = "hpsp://b6bc4badacf044a4ab0c08a8f052d0fe";
    
    g_pCallSessionInfo->url = [NSString stringWithFormat:@"hpsp://%@", g_pCallSessionInfo->sessionID];
    //_strUrl = "hpsp//";
    //_strUrl.append([g_pCallSessionInfo->sessionID UTF8String]);
    _strUrl = [g_pCallSessionInfo->url UTF8String];
    
    SdkConfig config = Sdk::Instance()->GetConfig();
    config.uid = [g_pUserInfo->uid UTF8String];
    
    config.videoEncode = SdkConfig::VideoEncode_S;
    int pullStreamId = 100;
    int previewStreamId = 101;
    Sdk::Instance()->Open(SdkOpenConnect, _strUrl.c_str(), pullStreamId, &config, previewStreamId);
    _facingBtn.text = (Sdk::Instance()->GetCameraFront() ? @"front" : @"back");
    _beautifyBtn.text = (Sdk::Instance()->GetCameraBeautify() ? @"beautify" : @"normal");
    
    //_previewView.hidden = !_previewView.isHidden;
    _previewView.hidden = NO;
    Sdk::Instance()->SetPushRecord(!Sdk::Instance()->GetPushRecord());
    _recordBtn.text = (Sdk::Instance()->GetPushRecord() ? @"recording" : @"record");
}

- (void)onStart:(id)gesture
{
    /*
    //_url = "rtmp://101.201.146.134/hulu/lishengcun";
    _url = "hpsp://b6bc4badacf044a4ab0c08a8f052d0fe";
    SdkConfig config = Sdk::Instance()->GetConfig();
    config.videoEncode = SdkConfig::VideoEncode_S;
    int pullStreamId = 100;
    int previewStreamId = 101;
    Sdk::Instance()->Open(SdkOpenConnect, _url, pullStreamId, &config, previewStreamId);
    _facingBtn.text = (Sdk::Instance()->GetCameraFront() ? @"front" : @"back");
    _beautifyBtn.text = (Sdk::Instance()->GetCameraBeautify() ? @"beautify" : @"normal");
     */
}

- (void)onStop:(id)gesture
{
    //send terminate call message to server
    
     MediaAppSignalMessage appMsg;
     MediaCallMessage *pCallMsg = appMsg.mutable__call();
     MediaCallMessage_CallTerminate *pTermMsg = pCallMsg->mutable__call_terminate();
     MediaCallMessage_BaseMesssage *pBaseMsg = pCallMsg->mutable__base();
     pBaseMsg->set__from([g_pUserInfo->uid UTF8String]);
     pBaseMsg->set__to([g_pCallSessionInfo->peer UTF8String]);
     pBaseMsg->set__portal([g_pUserInfo->app UTF8String]);
     
     pTermMsg->set__callid([g_pCallSessionInfo->sessionID UTF8String]);
     pTermMsg->set__reason(0);
     
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
    if(factSendLen!=sendLen){
        return;
    }
    
    _previewView.hidden = true;
    Sdk::Instance()->Close(_strUrl.c_str());
    
    //[self dismissViewControllerAnimated:YES completion:nil];
    if (g_pCallSessionInfo->eRoleType == E_AVCALL_CALLER) {
        g_curViewController = self.presentingViewController.presentingViewController;
        [self.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }else{
        g_curViewController = self.presentingViewController.presentingViewController;
        [self.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
    
    delete g_pCallSessionInfo;
    g_pCallSessionInfo = NULL;
    
}

-(void)StopCall{
    if (NULL==g_pCallSessionInfo) {
        return;
    }
    _previewView.hidden = true;
    Sdk::Instance()->Close(_strUrl.c_str());
    
    //[self dismissViewControllerAnimated:YES completion:nil];
    if (g_pCallSessionInfo->eRoleType == E_AVCALL_CALLER) {
        g_curViewController=self.presentingViewController.presentingViewController;
        [self.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }else{
        g_curViewController=self.presentingViewController.presentingViewController;
        [self.presentingViewController.presentingViewController dismissViewControllerAnimated:YES completion:nil];
    }
    
    delete g_pCallSessionInfo;
    g_pCallSessionInfo = NULL;
    
}
- (void)onRecord:(id)gesture
{
    _previewView.hidden = !_previewView.isHidden;
    Sdk::Instance()->SetPushRecord(!Sdk::Instance()->GetPushRecord());
    _recordBtn.text = (Sdk::Instance()->GetPushRecord() ? @"recording" : @"record");
}

- (void)onFacing:(id)gesture
{
    Sdk::Instance()->SetCameraFront(!Sdk::Instance()->GetCameraFront());
    _facingBtn.text = (Sdk::Instance()->GetCameraFront() ? @"front" : @"back");
}

- (void)onBeautify:(id)gesture
{
    Sdk::Instance()->SetCameraBeautify(!Sdk::Instance()->GetCameraBeautify());
    _beautifyBtn.text = (Sdk::Instance()->GetCameraBeautify() ? @"beauty" : @"normal");
}

@end
