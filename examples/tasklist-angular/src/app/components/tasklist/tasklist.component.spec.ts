import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {TasklistComponent} from './tasklist.component';
import {NgbPagination, NgbRadioGroup} from '@ng-bootstrap/ng-bootstrap';
import {FormsModule} from '@angular/forms';

import {TaskHelperService} from 'app/services/task.helper.service';
import {FieldNamePipe} from 'app/services/field-name.pipe';
import {FilterService} from 'app/services/filter.service';
import {SortableColumnComponent} from 'app/components/sorter/sortable-column.component';
import {ProcessHelperService} from 'app/services/process.helper.service';
import {provideStoreServiceMock} from '@ngxp/store-service/testing';
import {UserStoreService} from 'app/user/state/user.store-service';
import {of} from 'rxjs';

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
        provideStoreServiceMock(UserStoreService, {
          currentUserProfile$: {username: '', userIdentifier: '', fullName: ''}
        }),
        {provide: TaskHelperService, useValue: {tasks: of([])}},
        {provide: ProcessHelperService, useValue: {processes: of([])}}
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
