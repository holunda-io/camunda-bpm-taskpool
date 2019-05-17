import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserSelectionComponent} from './user-selection/user-selection.component';
import {UserStoreService} from './state/user.store-service';
import {StoreModule} from '@ngrx/store';
import {userReducer} from './state/user.reducer';
import {EffectsModule} from '@ngrx/effects';
import {UserEffects} from './state/user.effects';

@NgModule({
  declarations: [
    UserSelectionComponent
  ],
  providers: [
    UserStoreService
  ],
  imports: [
    CommonModule,
    EffectsModule.forFeature([UserEffects]),
    StoreModule.forFeature('user', userReducer)
  ],
  exports: [
    UserSelectionComponent
  ]
})
export class UserModule { }
