//
//  VCCallType.m
//  demo
//
//  Created by wlj on 2016/11/9.
//
//

#import <Foundation/Foundation.h>


#import <Foundation/Foundation.h>
#import "ViewController.h"
#import "VCCallType.h"
#import "VCRoomList.h"

@implementation VCCallType

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    UILabel* confBtn = [[UILabel alloc] init];
    confBtn.backgroundColor = [UIColor orangeColor];
    confBtn.text = @"Setting";
    confBtn.frame = CGRectMake(0, self.view.bounds.size.height-60, 120, 60);
    confBtn.textAlignment = NSTextAlignmentCenter;
    confBtn.autoresizingMask = UIViewAutoresizingFlexibleRightMargin;
    confBtn.userInteractionEnabled = YES;
    [confBtn addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickSettingBtn:)]];
    [self.view addSubview:confBtn];
    
    UILabel* p2pCall = [[UILabel alloc] init];
    p2pCall.backgroundColor = [UIColor orangeColor];
    p2pCall.text = @"p2pCall";
    p2pCall.frame = CGRectMake(self.view.bounds.size.width-120, self.view.bounds.size.height-60, 120, 60);
    p2pCall.textAlignment = NSTextAlignmentCenter;
    p2pCall.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    p2pCall.userInteractionEnabled = YES;
    [p2pCall addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickP2PBtn:)]];
    [self.view addSubview:p2pCall];
    
    // Do any additional setup after loading the view, typically from a nib.
}

-(void)onClickSettingBtn:(id)gesture{
    if (nil!=g_vcSetting) {
        g_vcSetting = [[VCSetting alloc] init];
    }

    [self presentViewController:g_vcSetting animated:YES completion:nil];
    g_curViewController = g_vcSetting;
    
    NSLog(@"presenting:%p cur:%p presented:%p", self.presentingViewController, self,  self.presentedViewController);
    
}

-(void)onClickP2PBtn:(id)gesture{

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
    
    //VCRoomList* vcRoomList = [[VCRoomList alloc] init];
    //g_vcRoomList = [[VCRoomList alloc] init];
    g_vcRoomList.viewData = userArray;
    [self presentViewController:g_vcRoomList animated:YES completion:nil];
    g_curViewController = g_vcRoomList;
}



@end
