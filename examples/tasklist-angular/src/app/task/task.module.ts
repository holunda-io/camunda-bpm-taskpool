import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {StoreModule} from '@ngrx/store';
import {taskReducer} from 'app/task/state/task.reducer';
import {EffectsModule} from '@ngrx/effects';
import {TaskEffects} from 'app/task/state/task.effects';
import {UserModule} from 'app/user/user.module';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    StoreModule.forFeature('task', taskReducer),
    EffectsModule.forFeature([TaskEffects]),
    UserModule
  ]
})
export class TaskModule { }
