import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserStoreService} from 'app/user/state/user.store-service';
import {ArchiveService} from 'tasklist/services';
import {DataEntriesLoaded, DataEntryActionTypes, LoadDataEntries} from 'app/dataentry/state/dataentry.actions';
import {catchError, filter, flatMap, map, withLatestFrom} from 'rxjs/operators';
import {DataEntry as DataEntryDto} from 'tasklist/models';
import {DataEntry} from 'app/dataentry/state/dataentry.reducer';
import {SelectUserAction, UserActionTypes} from 'app/user/state/user.actions';
import {StrictHttpResponse} from 'tasklist/strict-http-response';
import {of} from 'rxjs';

@Injectable()
export class DataentryEffects {

  public constructor(
    private archiveService: ArchiveService,
    private userStore: UserStoreService,
    private actions$: Actions) {
  }

  @Effect()
  loadDataEntriesOnUserSelect = this.actions$.pipe(
    ofType<SelectUserAction>(UserActionTypes.SelectUser),
    filter((action) => !!action.payload),
    map(() => new LoadDataEntries())
  );

  @Effect()
  loadDataEntries$ = this.actions$.pipe(
    ofType(DataEntryActionTypes.LoadDataEntries),
    withLatestFrom(this.userStore.userId$()),
    flatMap(([_, userId]) => this.archiveService.getBosResponse({
      filter: [''],
      XCurrentUserID: userId
    })),
    map(dataEntriesDtos => mapFromDto(dataEntriesDtos)),
    map(dataEntries => new DataEntriesLoaded(dataEntries)),
    catchError(err => {
      console.log('Error loading data entries:', err);
      return of();
    })
  );
}

function mapFromDto(dataEntryDtos: StrictHttpResponse<DataEntryDto[]>): DataEntry[] {
  return dataEntryDtos.body.map(dto => {
    return {
      name: dto.name,
      url: dto.url,
      description: dto.description,
      type: dto.type,
      payload: dto.payload
    };
  });
}
