import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {ApiModule, BASE_PATH} from 'tasklist';
import {AppComponent} from 'app/app.component';
import {TasklistComponent} from 'app/components/tasklist/tasklist.component';
import {TaskHelperService} from 'app/services/task.helper.service';
import {FieldNamePipe} from 'app/services/field-name.pipe';
import {FormsModule} from '@angular/forms';
import { FilterService } from './services/filter.service';


@NgModule({
  declarations: [
    AppComponent,
    TasklistComponent,
    FieldNamePipe
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    NgbModule,
    ApiModule
  ],
  providers: [
    TaskHelperService,
    FilterService,
    {provide: BASE_PATH, useValue: '/tasklist/rest'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
