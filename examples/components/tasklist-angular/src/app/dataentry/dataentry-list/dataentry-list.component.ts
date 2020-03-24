import {Component, OnInit} from '@angular/core';
import {DataentryStoreService} from 'app/dataentry/state/dataentry.store-service';
import {Observable} from 'rxjs';
import {DataEntry} from 'app/dataentry/state/dataentry.reducer';
import {UserStoreService} from 'app/user/state/user.store-service';
import {UserProfile} from 'app/user/state/user.reducer';

@Component({
  selector: 'data-entry-list',
  templateUrl: './dataentry-list.component.html',
  styleUrls: ['dataentry-list.component.scss']
})
export class DataentryListComponent implements OnInit {

  dataEntries$: Observable<DataEntry[]>;
  currentProfile$: Observable<UserProfile>;
  currentDataTab = 'description';
  itemsPerPage: number;
  totalItems: any;
  page: number;

  constructor(
    private dataEntryStore: DataentryStoreService,
    private userStore: UserStoreService
  ) {  }

  ngOnInit() {
    this.dataEntries$ = this.dataEntryStore.dataEntries$();
    this.currentProfile$ = this.userStore.currentUserProfile$();
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
