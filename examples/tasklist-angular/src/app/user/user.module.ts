import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {UserSelectionComponent} from "app/user/user-selection/user-selection.component";
import {UserStoreService} from "app/user/state/user.store-service";
import {StoreModule} from "@ngrx/store";
import {userReducer} from "app/user/state/user.reducer";
import {EffectsModule} from "@ngrx/effects";
import {UserEffects} from "app/user/state/user.effects";

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
