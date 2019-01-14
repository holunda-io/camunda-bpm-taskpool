import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import 'rxjs/add/observable/of';
import { ApproveTaskComponent } from './approve-task.component';
import { Observable } from 'rxjs-compat';
import { NgbPagination, NgbRadioGroup } from '@ng-bootstrap/ng-bootstrap';
import { FormsModule } from '@angular/forms';
import { ApproveRequestService } from 'process/api/approveRequest.service';
import { ActivatedRoute } from '@angular/router';


describe('Component: ApproveTaskComponent', () => {

  const taskId = '4711';
  let component: ApproveTaskComponent;
  let fixture: ComponentFixture<ApproveTaskComponent>;

  beforeEach(async(() => {

    const approveRequestServiceSpy = jasmine.createSpyObj('ApproveRequestService', {
      'loadTaskApproveRequestFormData': Observable.of({
        approvalRequest: {},
        task: {},
      }),
      'submitTaskApproveRequestSubmitData': Observable.of({}),
    });

    TestBed.configureTestingModule({
      imports: [
        FormsModule
      ],
      declarations: [
        ApproveTaskComponent,
      ],
      providers: [
        { provide: ApproveRequestService, useValue: approveRequestServiceSpy },
        { provide: ActivatedRoute, useValue: {
            snapshot: {
              paramMap: {get: () => taskId},
            }
          },
        },
      ],
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
