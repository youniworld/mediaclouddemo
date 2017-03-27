//
//  VCRoomList.h
//  demo
//
//  Created by wlj on 2016/11/9.
//
//


#import <UIKit/UIKit.h>

@interface VCRoomList : UIViewController <UITableViewDataSource, UITableViewDelegate>{
    
    
    UITableView* _tableView;
}

@property (strong, nonatomic) NSMutableArray* viewData;

-(void) changeUserState:(const char*) uid State:(uint32_t)state;
-(void) refreshUserState;
-(void) ProcessCallComing;

@end

