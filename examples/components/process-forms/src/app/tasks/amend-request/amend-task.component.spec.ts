import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import 'rxjs/add/observable/of';
import { AmendTaskComponent } from './amend-task.component';
import { Observable } from 'rxjs-compat';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { UserTaskAmendRequestService } from 'process/services/user-task-amend-request.service';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { of } from 'rxjs-compat/observable/of';

describe('Component: ApproveTaskComponent', () => {

  const taskId = '4711';
  let component: AmendTaskComponent;
  let fixture: ComponentFixture<AmendTaskComponent>;

  beforeEach(waitForAsync(() => {

    const amendRequestServiceSpy = jasmine.createSpyObj('AmendRequestService', {
      'loadTaskAmendRequestFormData': of({
        approvalRequest: {},
        task: {},
      }),
      'submitTaskAmendRequestSubmitData': of({}),
    });
    const envSpy = jasmine.createSpyObj('EnvironmentHelperService', {
      'env': of({
        applicationName: 'foo',
        tasklistUrl: 'http://bar',
      }),
      'none' : {
        applicationName: 'foo',
        tasklistUrl: 'http://bar',
      }
    });


    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        AmendTaskComponent,
      ],
      providers: [
        { provide: Router, useValue: jasmine.createSpyObj('Router', { 'navigate': {} }) },
        { provide: UserTaskAmendRequestService, useValue: amendRequestServiceSpy },
        { provide: ActivatedRoute, useValue: {
            snapshot: {
              paramMap: {get: () => taskId},
              queryParams: { 'userId': 'some-id'}
            }
          },
        },
        { provide: EnvironmentHelperService, useValue: envSpy }
      ],
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(AmendTaskComponent);

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
