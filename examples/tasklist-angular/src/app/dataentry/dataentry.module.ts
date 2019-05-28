import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {dataentryReducer} from 'app/dataentry/state/dataentry.reducer';
import {StoreModule} from '@ngrx/store';
import {EffectsModule} from '@ngrx/effects';
import {UserModule} from 'app/user/user.module';
import {DataentryListComponent} from 'app/dataentry/dataentry-list/dataentry-list.component';
import {DataentryEffects} from 'app/dataentry/state/dataentry.effects';
import {DataentryStoreService} from 'app/dataentry/state/dataentry.store-service';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule} from '@angular/forms';
import {ServiceModule} from 'app/services/service.module';

@NgModule({
  declarations: [
    DataentryListComponent,
  ],
  imports: [
    CommonModule,
    NgbModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    EffectsModule.forFeature([DataentryEffects]),
    StoreModule.forFeature('dataentry', dataentryReducer),
    ServiceModule,
    UserModule,
  ],
  exports: [
    DataentryListComponent
  ],
  providers: [
    DataentryStoreService,
  ]
})
export class DataEntryModule {
}
