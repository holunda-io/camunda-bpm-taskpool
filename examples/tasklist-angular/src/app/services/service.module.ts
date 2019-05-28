import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {FormsModule} from '@angular/forms';
import {FieldNamePipe} from 'app/services/field-name.pipe';

@NgModule({
  declarations: [
    FieldNamePipe,
  ],
  imports: [
    CommonModule,
    NgbModule,
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
  ],
  exports: [
    FieldNamePipe
  ],
  providers: [

  ]
})
export class ServiceModule {
}
