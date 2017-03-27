//
//  ViewController3.h
//  demo
//
//  Created by lishengcun on 16/8/15.
//
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVCaptureOutput.h>

@interface ViewController3 : UIViewController <AVCaptureVideoDataOutputSampleBufferDelegate>

-(void)StartCall;
-(void)StopCall;

@end
