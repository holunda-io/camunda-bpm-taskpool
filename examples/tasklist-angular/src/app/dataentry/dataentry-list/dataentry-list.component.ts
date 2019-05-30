import {Component, OnInit} from '@angular/core';
import {DataentryStoreService} from 'app/dataentry/state/dataentry.store-service';
import {Observable} from 'rxjs';
import {DataEntry} from 'app/dataentry/state/dataentry.reducer';

@Component({
  selector: 'data-entry-list',
  templateUrl: './dataentry-list.component.html'
})
export class DataentryListComponent implements OnInit {

  dataEntries$: Observable<DataEntry[]>;
  currentDataTab = 'description';
  itemsPerPage: number;
  totalItems: any;
  page: number;

  constructor(
    private dataEntryStore: DataentryStoreService
  ) {
    this.dataEntries$ = this.dataEntryStore.dataEntries$();
  }

  ngOnInit() {

  }

  toFieldSet(payload: any) {
    const payloadProps = Object.keys(payload);
    const result = [];
    for (const prop of payloadProps) {
      result.push({ name: prop, value: payload[prop] });
    }
    return result;
  }

  reload() {
    console.log('Reload');
  }

  loadPage() {
    console.log('Load page');
  }
}
