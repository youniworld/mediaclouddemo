//
//  VCRoomList.m
//  demo
//
//  Created by wlj on 2016/11/9.
//
//

#import <Foundation/Foundation.h>

#import "ViewController.h"
#import "VCRoomList.h"
#import "mediaappsingnal.pb.h"
#import "ViewController.h"
#import "VCCallInvite.h"


@implementation VCRoomList

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor whiteColor];
   
    UILabel* lbSetting = [[UILabel alloc] init];
    lbSetting.backgroundColor = [UIColor orangeColor];
    lbSetting.text = @"Setting";
    lbSetting.frame = CGRectMake(self.view.bounds.size.width-120, self.view.bounds.size.height-60, 120, 60);
    lbSetting.textAlignment = NSTextAlignmentCenter;
    lbSetting.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    lbSetting.userInteractionEnabled = YES;
    [lbSetting addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickSetting:)]];
    [self.view addSubview:lbSetting];
    
    
    UILabel* lbBack = [[UILabel alloc] init];
    lbBack.backgroundColor = [UIColor orangeColor];
    lbBack.text = @"Back";
    lbBack.frame = CGRectMake(0, self.view.bounds.size.height-60, 120, 60);
    lbBack.textAlignment = NSTextAlignmentCenter;
    lbBack.autoresizingMask = UIViewAutoresizingFlexibleLeftMargin;
    lbBack.userInteractionEnabled = YES;
    [lbBack addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onClickBack:)]];
    lbBack.hidden = YES;
    [self.view addSubview:lbBack];
    
    _tableView = [[UITableView alloc] initWithFrame:CGRectMake(0, 20, self.view.bounds.size.width, self.view.bounds.size.height-100) style:UITableViewStylePlain];
    _tableView.delegate = self;
    _tableView.dataSource=self;
    
    [self.view addSubview:_tableView];
    
    //  [_tableView reloadData];
    
    // Do any additional setup after loading the view, typically from a nib.
}
-(void) refreshUserState{
    uint32_t count = [self.viewData count];
    for(uint32_t i=0; i<count; i++){
       // UserInfo* userInfo = [self.viewData objectAtIndex:i];
        NSIndexPath *indexPath = [NSIndexPath indexPathForRow:i inSection:0];
        [_tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationNone];
    }
}

-(void)onClickSetting:(id)gesture{
    if (nil!=g_vcSetting) {
        g_vcSetting = [[VCSetting alloc] init];
    }
    
    [self presentViewController:g_vcSetting animated:YES completion:nil];
    g_curViewController = g_vcSetting;
}

-(void)onClickBack:(id)gesture{
    g_curViewController = self.presentingViewController;
   [self dismissViewControllerAnimated:YES completion:nil];
}

-(NSInteger)tableView:(UITableView*)tableView numberOfRowsInSection:(NSInteger)section{
    NSInteger cnt = [self.viewData count];
    return cnt;
}

-(UITableViewCell*)tableView:(UITableView*)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString* cellIdentifier = @"cellID";
    UITableViewCell* cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if(cell==nil){
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
   // NSString* strForCell=[self.viewData objectAtIndex:indexPath.row];
   // [cell.textLabel setText:strForCell];
    
    UserInfo *userinfo =[self.viewData objectAtIndex:indexPath.row];
    NSString* strForCell = userinfo.uid;
    [cell.textLabel setText:strForCell];
    if (userinfo.state>0) {
        cell.textLabel.textColor = [UIColor brownColor];
    }else{
        cell.textLabel.textColor = [UIColor blackColor];
    }
    
    NSLog(@"info section:%d rod:%ld\n", indexPath.section, (long)indexPath.row);
    
    return cell;
}

-(void) ProcessCallComing{
    if (nil!=g_vcCallComing) {
        g_vcCallComing =[[VCCallComing alloc] init];
    }
    
    [self presentViewController:g_vcCallComing animated:YES completion:nil];
    [g_vcCallComing setCallerInfo:[g_pCallSessionInfo->peer UTF8String]];
    g_curViewController = g_vcCallComing;
}

-(void) changeUserState:(const char*) uid State:(uint32_t)state{
    uint32_t count = [self.viewData count];
    for(uint32_t i=0; i<count; i++){
        UserInfo* userInfo = [self.viewData objectAtIndex:i];
        if (0==strcmp(uid, [userInfo.uid UTF8String])) {
            [userInfo setState:state];
         //   NSIndexPath *indexPath = [NSIndexPath indexPathForRow:i inSection:0];
         //   [_tableView reloadRowsAtIndexPaths:[NSArray arrayWithObjects:indexPath,nil] withRowAnimation:UITableViewRowAnimationNone];
        }
    }
    [_tableView reloadData];
}

-(void) scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate{
 
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
    self.viewData = userArray;
    [_tableView reloadData];
    
    NSLog(@"scroll view did scroll end dragging............");
}

-(void)tableView:(UITableView*)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    UITableViewCell* cell = [tableView cellForRowAtIndexPath:indexPath];
    
    NSLog(@"selection:%d row:%ld data is:%@", indexPath.section, indexPath.row, cell.textLabel.text);
    
    //send create session http request
    NSString *strUrl = @"http://lianmaibiz.hifun.mobi:9800/mediasession/create";
    NSURL *url = [[NSURL alloc] initWithString:strUrl];
    
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url cachePolicy:NSURLRequestReloadIgnoringCacheData timeoutInterval:5];
    [request setValue:g_pUserInfo->token forHTTPHeaderField:@"token"];
    [request setValue:g_pUserInfo->app forHTTPHeaderField:@"portal"];
    
    //NSURLResponse *rsp=nil;
    //NSError *error=nil;
    NSHTTPURLResponse *rsp=nil;
    NSError *error=nil;
    NSData *result = [NSURLConnection sendSynchronousRequest:request returningResponse:&rsp error:&error];
    if(error!=nil){
        NSString *errDesc = [error localizedDescription];
        NSLog(@"http create media session failed. err:%@", errDesc);
        return;
    }
    
    NSString *html = [[NSString alloc] initWithData:result encoding:NSUTF8StringEncoding];
    NSHTTPURLResponse *httprsp = (NSHTTPURLResponse*)rsp;
    NSInteger statusCode = [httprsp statusCode];
    if (200!=statusCode) {
        NSLog(@"http create media session failed. statuscode:%d", statusCode);
        return;
    }

    id jsonObj = [NSJSONSerialization JSONObjectWithData:result options:NSJSONReadingMutableContainers error:&error];
    
    if(!([jsonObj isKindOfClass:[NSDictionary class]])){
        NSLog(@"http create media session failed. body:%@", html);
        return;
    }
    
    NSDictionary *dicRsp = (NSDictionary*)jsonObj;
    NSNumber *errCode = [dicRsp valueForKey:@"errcode"];
    NSString *sid = [dicRsp valueForKey:@"sessionid"];
    
    if (0!=[errCode intValue]) {
        NSLog(@"http create media session errcode:%d", [errCode intValue]);
        return;
    }
    
    if (NULL!=g_pCallSessionInfo) {
        delete g_pCallSessionInfo;
    }
    g_pCallSessionInfo = new T_CALLSESSIONINFO;
    
    g_pCallSessionInfo->sessionID = sid;
    g_pCallSessionInfo->peer = cell.textLabel.text;
    g_pCallSessionInfo->eRoleType = E_AVCALL_CALLER;
    
    NSLog(@"http create media session successed. sid:%@", g_pCallSessionInfo->sessionID);
   
    //send invite request by tcp connection
    MediaAppSignalMessage appMsg;
    MediaCallMessage *pCallMsg = appMsg.mutable__call();
    
    MediaCallMessage_BaseMesssage *pBaseMsg = pCallMsg->mutable__base();
    pBaseMsg->set__from([g_pUserInfo->uid UTF8String]);
    pBaseMsg->set__to([cell.textLabel.text UTF8String]);
    pBaseMsg->set__portal([g_pUserInfo->app UTF8String]);
    
    MediaCallMessage_CallInitiate *pInitMsg = pCallMsg->mutable__call_initiate();
    pInitMsg->set__caller([g_pUserInfo->uid UTF8String]);
    pInitMsg->set__callid([g_pCallSessionInfo->sessionID UTF8String]);
    pInitMsg->set__media_session([g_pCallSessionInfo->sessionID UTF8String]);
    
    uint32_t iMsgLen = appMsg.ByteSize();
    
    UInt8 sendBuf[MAX_STREAM_BUFFER_SIZE];
    appMsg.SerializeToArray(sendBuf+6, iMsgLen);
    pCallMsg->set_allocated__call_initiate(NULL);
    pCallMsg->set_allocated__base(NULL);
    appMsg.set_allocated__call(NULL);
    
    sendBuf[0] = 0xFA;
    sendBuf[1] = 0xAF;
    uint32_t iMsgLenBigEnd = htonl(iMsgLen);
    memcpy(sendBuf+2, &iMsgLenBigEnd, 4);
    
    CFIndex sendLen = 6+iMsgLen;
    CFIndex factSendLen = CFWriteStreamWrite(g_writeStream, sendBuf, sendLen);
    NSLog(@"send initialize message to server. callee:%@ msglen:%d", cell.textLabel.text,  factSendLen);
    
    if (nil==g_vcCallInvite) {
        g_vcCallInvite = [[VCCallInvite alloc]init];
    }
    
    [self presentViewController:g_vcCallInvite animated:YES completion:nil];
    [g_vcCallInvite setTips:[NSString stringWithFormat:@"calling %@", cell.textLabel.text]];
    g_curViewController = g_vcCallInvite;
}


@end

