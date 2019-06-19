import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import 'rxjs/add/observable/of';
import {TasklistComponent} from './tasklist.component';
import {Observable} from 'rxjs-compat';
import {NgbPagination, NgbRadioGroup} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule} from '@angular/forms';

import {TaskHelperService} from 'app/services/task.helper.service';
import {FieldNamePipe} from 'app/services/field-name.pipe';
import {FilterService} from 'app/services/filter.service';
import {ProfileHelperService} from 'app/services/profile.helper.service';
import {SortableColumnComponent} from 'app/components/sorter/sortable-column.component';
import {ProcessHelperService} from 'app/services/process.helper.service';

describe('Component: TasklistComponent', () => {

  let component: TasklistComponent;
  let fixture: ComponentFixture<TasklistComponent>;

  beforeEach(async(() => {

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        TasklistComponent,
        NgbPagination,
        NgbRadioGroup,
        FieldNamePipe,
        SortableColumnComponent
      ],
      providers: [
        FilterService,
        {
          provide: ProfileHelperService, useValue: {
            currentProfile$: Observable.of([]),
            noProfile: {username: '', userIdentifier: '', fullName: ''}
          }
        },
        {provide: TaskHelperService, useValue: {tasks: Observable.of([])}},
        {provide: ProcessHelperService, useValue: {processes: Observable.of([])}}
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(TasklistComponent);

      // get test component from the fixture
      component = fixture.componentInstance;

      // detect changes
      fixture.detectChanges();
    });
  }));

  it('should create', () => {
    expect(component).toBeDefined();
  });

});
