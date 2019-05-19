import {Injectable} from '@angular/core';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {UserStoreService} from 'app/user/state/user.store-service';
import {ProcessService} from 'tasklist/services';
import {ProcessActionTypes, StartableProcessDefinitionsLoaded} from 'app/process/state/process.actions';
import {flatMap, map, withLatestFrom} from 'rxjs/operators';
import {ProcessDefinition as ProcessDto} from 'tasklist/models';
import {ProcessDefinition} from 'app/process/state/process.reducer';

@Injectable()
export class ProcessEffects {

  public constructor(
    private processService: ProcessService,
    private userStore: UserStoreService,
    private actions$: Actions) {
  }

  @Effect()
  loadStartableProcesses$ = this.actions$.pipe(
    ofType(ProcessActionTypes.LoadStartableProcesses),
    withLatestFrom(this.userStore.userId$()),
    flatMap(([_, userId]) => this.processService.getStartableProcesses(userId)),
    map(procDtos => mapFromDto(procDtos)),
    map(procDefs => new StartableProcessDefinitionsLoaded(procDefs))
  );
}

function mapFromDto(processDtos: ProcessDto[]): ProcessDefinition[] {
  return processDtos.map(dto => {
    return {key: dto.definitionKey}
  })
}
