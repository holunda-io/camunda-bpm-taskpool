import {NgModule} from '@angular/core';
import {CommonModule, KeyValuePipe} from '@angular/common';
import {UserSelectionComponent} from './user-selection/user-selection.component';
import {UserStoreService} from './state/user.store-service';
import {StoreModule} from '@ngrx/store';
import {userReducer} from './state/user.reducer';
import {EffectsModule} from '@ngrx/effects';
import {UserEffects} from './state/user.effects';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  declarations: [
    UserSelectionComponent
  ],
  providers: [
    UserStoreService
  ],
  imports: [
    CommonModule,
    NgbModule,
    EffectsModule.forFeature([UserEffects]),
    StoreModule.forFeature('user', userReducer)
  ],
  exports: [
    UserSelectionComponent
  ]
})
export class UserModule { }
