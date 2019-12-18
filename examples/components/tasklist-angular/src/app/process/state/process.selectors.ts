import {createSelector} from '@ngrx/store';
import {ProcessDefinition, ProcessState} from 'app/process/state/process.reducer';

export interface StateWithProcesses {
  process: ProcessState;
}

const selectFeature = (state: StateWithProcesses) => state.process;

export const startableProcesses = createSelector(
  selectFeature,
  (state: ProcessState): ProcessDefinition[] => state.startableProcesses
);
