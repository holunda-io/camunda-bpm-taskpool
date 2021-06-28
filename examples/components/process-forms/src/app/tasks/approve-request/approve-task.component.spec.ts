import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import 'rxjs/add/observable/of';
import { ApproveTaskComponent } from './approve-task.component';
import { FormsModule } from '@angular/forms';
import { UserTaskApproveRequestService } from 'process/services/user-task-approve-request.service';
import { ActivatedRoute, Router } from '@angular/router';
import { EnvironmentHelperService } from 'app/services/environment.helper.service';
import { of } from 'rxjs-compat/observable/of';


describe('Component: ApproveTaskComponent', () => {

  const taskId = '4711';
  let component: ApproveTaskComponent;
  let fixture: ComponentFixture<ApproveTaskComponent>;

  beforeEach(waitForAsync(() => {

    const approveRequestServiceSpy = jasmine.createSpyObj('ApproveRequestService', {
      loadTaskApproveRequestFormData: of({
        approvalRequest: {},
        task: {}
      }),
      submitTaskApproveRequestSubmitData: of({})
    });
    const envSpy = jasmine.createSpyObj('EnvironmentHelperService', {
      env: of({
        applicationName: 'foo',
        tasklistUrl: 'http://bar'
      }),
      none: {
        applicationName: 'foo',
        tasklistUrl: 'http://bar'
      }
    });

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        ApproveTaskComponent
      ],
      providers: [
        { provide: Router, useValue: jasmine.createSpyObj('Router', { 'navigate': {} }) },
        { provide: UserTaskApproveRequestService, useValue: approveRequestServiceSpy },
        {
          provide: ActivatedRoute, useValue: {
            snapshot: {
              paramMap: { get: () => taskId },
              queryParams: { 'userId': 'some-id' }
            }
          }
        },
        { provide: EnvironmentHelperService, useValue: envSpy }
      ]
    }).compileComponents().then(() => {
      // create component and test fixture
      fixture = TestBed.createComponent(ApproveTaskComponent);

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
