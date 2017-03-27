//
//  ViewController2.m
//  demo
//
//  Created by lishengcun on 16/5/17.
//
//

#import "ViewController2.h"
#import "sdk/Sdk.h"
#import "ios/IosVideoView.h"

@interface ViewController2 ()
{
    VideoView* videoView;
    VideoView* videoView2;
}
@end

@implementation ViewController2

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    
    UILabel* startBtn = [[UILabel alloc] init];
    startBtn.backgroundColor = [UIColor brownColor];
    startBtn.text = @"start";
    startBtn.frame = CGRectMake(0, 20, 100, 60);
    startBtn.textAlignment = NSTextAlignmentCenter;
    startBtn.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    startBtn.userInteractionEnabled = YES;
    [startBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onStart:)]];
    [self.view addSubview:startBtn];
    
    UILabel* stopBtn = [[UILabel alloc] init];
    stopBtn.backgroundColor = [UIColor orangeColor];
    stopBtn.text = @"stop";
    stopBtn.frame = CGRectMake(self.view.bounds.size.width-100, 20, 100, 60);
    stopBtn.textAlignment = NSTextAlignmentCenter;
    stopBtn.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    stopBtn.userInteractionEnabled = YES;
    [stopBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onStop:)]];
    [self.view addSubview:stopBtn];
    
    CGRect frame = CGRectMake(0, 20+60, self.view.bounds.size.width, self.view.bounds.size.height-20-60-100);
    videoView = [[VideoView alloc] initWithFrame:frame];
    videoView.backgroundColor = [UIColor grayColor];
    videoView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    videoView.streamId = 0;
    videoView.mode = ViewScaleModeFill;
    [self.view addSubview:videoView];
    
//    frame = CGRectMake(0, self.view.bounds.size.height-100, 100, 100);
//    videoView2 = [[VideoView alloc] initWithFrame:frame];
//    videoView2.backgroundColor = [UIColor grayColor];
//    videoView2.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleTopMargin;
//    videoView2.streamId = 0;
//    videoView2.mode = ViewScaleModeFill;
//    [self.view addSubview:videoView2];
}

- (void)dealloc
{
}
#undef __KARAOKE__

- (void)onStart:(id)gesture
{
    Sdk::Instance()->Open(SdkOpenPull, "hpsp://b6bc4badacf044a4ab0c08a8f052d0fe", 0);
    //Sdk::Instance()->Open("rtmp://101.201.146.134/hulu/wt", true, 0);
    //Sdk::Instance()->Open("rtmp://101.201.146.134/hulu/lishengcun", true, 0);
#ifdef __KARAOKE__
    NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDesktopDirectory, NSUserDomainMask, YES);
    NSString* thepath = [paths lastObject];
    thepath = [thepath stringByAppendingPathComponent:@"a0.mp3"];
    
    // 沙盒Documents目录
    NSString * appDir = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    
    
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *filePath = [appDir stringByAppendingPathComponent:@"a0.mp3"];
    if(![fileManager fileExistsAtPath:filePath]) //如果不存在
    {
        NSError *error;
        BOOL filesPresent = [fileManager copyItemAtPath:thepath toPath:appDir  error:&error];
        //        BOOL filesPresent = [self copyFile:thepath toPath:appDir];
        if (filesPresent) {
            NSLog(@"Copy Success");
        }
        else
        {
            NSLog(@"Copy Fail");
        }
    }
    else
    {
        NSLog(@"文件已存在");
    }
    const char *ptr2 = [filePath cStringUsingEncoding:NSASCIIStringEncoding];
    
    AudioFilterEffect filterEffect{90,128,128,6};
    Sdk::Instance()->OpenKaraoke(ptr2,&filterEffect);

#endif
}

- (void)onStop:(id)gesture
{
    Sdk::Instance()->Close("hpsp://b6bc4badacf044a4ab0c08a8f052d0fe");
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
