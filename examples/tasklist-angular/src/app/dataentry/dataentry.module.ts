import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {dataentryReducer} from 'app/dataentry/state/dataentry.reducer';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {UserModule} from 'app/user/user.module';

import {DataentryEffects} from 'app/dataentry/state/dataentry.effects';
import {DataentryStoreService} from 'app/dataentry/state/dataentry.store-service';
import {ServiceModule} from 'app/services/service.module';

@NgModule({
  declarations: [

  ],
  imports: [
    CommonModule,
    EffectsModule.forFeature([DataentryEffects]),
    StoreModule.forFeature('dataentry', dataentryReducer),
    ServiceModule,
    UserModule,
  ],
  exports: [
  ],
  providers: [
    DataentryStoreService,
  ]
})
export class DataEntryModule {
}
