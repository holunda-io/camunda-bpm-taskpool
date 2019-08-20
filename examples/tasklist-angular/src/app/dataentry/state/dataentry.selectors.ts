import {createSelector} from '@ngrx/store';
import {DataEntry, DataEntryState} from 'app/dataentry/state/dataentry.reducer';

export interface StateWithDataEntries {
  archive: DataEntryState;
}

const selectFeature = (state: StateWithDataEntries) => state.archive;

export const dataEntries = createSelector(
  selectFeature,
  (state: DataEntryState): DataEntry[] => state.dataEntries
);
