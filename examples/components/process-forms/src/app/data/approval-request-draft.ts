import {ApprovalRequestDraft} from 'process/models/approval-request-draft';

export const empty: ApprovalRequestDraft = {
  amount: 0.00,
  currency: 'EUR',
  applicant: '',
  subject: ''
};

export const sabbatical: ApprovalRequestDraft = {
  amount: 0.00,
  currency: 'EUR',
  applicant: 'ironman',
  subject: 'Sabbatical'
};

export const advancedTraining: ApprovalRequestDraft = {
  amount: 900.00,
  currency: 'EUR',
  applicant: 'hulk',
  subject: 'Advanced training'
};

export const businessTrip: ApprovalRequestDraft = {
  amount: 1400.00,
  currency: 'USD',
  applicant: 'hulk',
  subject: 'Business Trip'
};
