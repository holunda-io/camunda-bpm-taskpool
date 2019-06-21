import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ProcesslistComponent} from 'app/process/process-list/process-list.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {ProcessStoreService} from 'app/process/state/process.store-service';
import {processReducer} from 'app/process/state/process.reducer';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {ProcessEffects} from 'app/process/state/process.effects';
import {UserModule} from 'app/user/user.module';
import {SharedModule} from 'app/shared/shared.module';

@NgModule({
  declarations: [ProcesslistComponent],
  imports: [
    CommonModule,
    NgbModule,
    EffectsModule.forFeature([ProcessEffects]),
    StoreModule.forFeature('process', processReducer),
    UserModule,
    SharedModule
  ],
  exports: [
    ProcesslistComponent
  ],
  providers: [
    ProcessStoreService
  ]
})
export class ProcessModule {
}
